package com.ecovolt.demo.serviceimpl;

import com.ecovolt.demo.dtos.request.InicioSesionSolicitudDto;
import com.ecovolt.demo.dtos.request.RegistroUsuarioDto;
import com.ecovolt.demo.dtos.request.ReenviarVerificacionDto;
import com.ecovolt.demo.dtos.request.VerificarCorreoDto;
import com.ecovolt.demo.dtos.response.InicioSesionRespuestaDto;
import com.ecovolt.demo.dtos.response.ReniecRespuesta;
import com.ecovolt.demo.dtos.response.VerificacionEnviadaRespuestaDto;
import com.ecovolt.demo.entities.Casa;
import com.ecovolt.demo.entities.Habitacion;
import com.ecovolt.demo.entities.Historico;
import com.ecovolt.demo.entities.Rol;
import com.ecovolt.demo.entities.Usuario;
import com.ecovolt.demo.entities.DispositivoVirtual;
import com.ecovolt.demo.Enums.TipoUsuario;
import com.ecovolt.demo.exceptions.BadRequestException;
import com.ecovolt.demo.exceptions.ResourceNotFoundException;
import com.ecovolt.demo.repositories.CasaRepositorio;
import com.ecovolt.demo.repositories.HabitacionRepositorio;
import com.ecovolt.demo.repositories.HistoricoRepositorio;
import com.ecovolt.demo.repositories.RolRepositorio;
import com.ecovolt.demo.repositories.UsuarioRepositorio;
import com.ecovolt.demo.repositories.DispositivoVirtualRepositorio;
import com.ecovolt.demo.security.JwtService;
import com.ecovolt.demo.services.feingservice.ReniecClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AutenticacionService {

    private static final int TOKEN_EXPIRATION_HOURS = 24;
    private static final long VERIFICATION_TOKEN_EXPIRATION_MILLIS = TOKEN_EXPIRATION_HOURS * 60L * 60L * 1000L;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final CasaRepositorio casaRepositorio;
    private final HabitacionRepositorio habitacionRepositorio;
    private final DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    private final HistoricoRepositorio historicoRepositorio;
    private final ReniecClient reniecClient;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    @Value("${api.token}")
    private String apiToken;

    public AutenticacionService(UsuarioRepositorio usuarioRepositorio,
                                RolRepositorio rolRepositorio,
                                CasaRepositorio casaRepositorio,
                                HabitacionRepositorio habitacionRepositorio,
                                DispositivoVirtualRepositorio dispositivoVirtualRepositorio,
                                HistoricoRepositorio historicoRepositorio,
                                ReniecClient reniecClient,
                                PasswordEncoder passwordEncoder,
                                AuthenticationManager authenticationManager,
                                JwtService jwtService,
                                ModelMapper modelMapper) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.casaRepositorio = casaRepositorio;
        this.habitacionRepositorio = habitacionRepositorio;
        this.dispositivoVirtualRepositorio = dispositivoVirtualRepositorio;
        this.historicoRepositorio = historicoRepositorio;
        this.reniecClient = reniecClient;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.modelMapper = modelMapper;
    }

    public InicioSesionRespuestaDto login(InicioSesionSolicitudDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizeEmail(request.getCorreo()),
                        request.getContrasena()
                )
        );
        String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
        return new InicioSesionRespuestaDto(token, "Bearer", jwtService.getExpirationSeconds());
    }

    @Transactional
    public VerificacionEnviadaRespuestaDto register(RegistroUsuarioDto request) {
        String correo = normalizeEmail(request.getCorreo());
        String dni = request.getDni().trim();

        if (usuarioRepositorio.existsByCorreo(correo)) {
            throw new BadRequestException("El correo ya se encuentra registrado");
        }

        if (usuarioRepositorio.existsByDni(dni)) {
            throw new BadRequestException("El DNI ya se encuentra registrado");
        }

        ReniecRespuesta reniecResponse = findPersonByDni(dni);

        Usuario usuario = modelMapper.map(request, Usuario.class);
        usuario.setDni(dni);
        usuario.setNombre(reniecResponse.getFirstName());
        usuario.setApellido(buildLastNames(reniecResponse));
        usuario.setCorreo(correo);
        usuario.setUsername(buildUsername(reniecResponse));
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setNombreEmpresa(trimToNull(request.getNombreEmpresa()));
        usuario.setRuc(trimToNull(request.getRuc()));
        usuario.setActivo(false);
        assignVerificationToken(usuario);
        usuario.getRoles().add(findOrCreateRole(usuario.getTipoUsuario().name()));

        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);

        if (usuarioGuardado.getTipoUsuario() == TipoUsuario.PERSONAL) {
            createDemoHome(usuarioGuardado);
        }

        return simulateVerificationEmail(usuarioGuardado);
    }

    @Transactional
    public void verifyEmail(VerificarCorreoDto request) {
        Usuario usuario = usuarioRepositorio.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("El token de verificacion no es valido"));

        if (!isVerificationTokenValid(request.getToken(), usuario)) {
            throw new BadRequestException("El token de verificacion ha expirado");
        }

        usuario.setActivo(true);
        usuario.setVerificationToken(null);
        usuario.setVerificationTokenExpiresAt(null);
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public VerificacionEnviadaRespuestaDto resendVerification(ReenviarVerificacionDto request) {
        Usuario usuario = usuarioRepositorio.findByCorreo(normalizeEmail(request.getCorreo()))
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con el correo indicado"));

        if (usuario.isActivo()) {
            throw new BadRequestException("La cuenta ya se encuentra activa");
        }

        assignVerificationToken(usuario);
        Usuario usuarioGuardado = usuarioRepositorio.save(usuario);

        return simulateVerificationEmail(usuarioGuardado);
    }

    private void createDemoHome(Usuario usuario) {
        Casa casa = casaRepositorio.save(Casa.builder()
                .nombre("Hogar virtual de ejemplo")
                .usuario(usuario)
                .build());

        Habitacion habitacion = habitacionRepositorio.save(Habitacion.builder()
                .nombre("Sala principal")
                .casa(casa)
                .build());

        createDemoDevice(habitacion, "Luz LED", "luz", 10.0, 5);
        createDemoDevice(habitacion, "TV Smart", "TV", 120.0, 4);
        createDemoDevice(habitacion, "Refrigerador", "refrigerador", 150.0, 8);
    }

    private void createDemoDevice(Habitacion habitacion, String nombre, String tipo, double watts, int horasUso) {
        DispositivoVirtual dispositivo = dispositivoVirtualRepositorio.save(DispositivoVirtual.builder()
                .nombre(nombre)
                .tipo(tipo)
                .potenciaWatts(watts)
                .activo(false)
                .automatico(false)
                .eliminado(false)
                .habitacion(habitacion)
                .build());

        historicoRepositorio.saveAll(buildInitialHistory(dispositivo, horasUso));
    }

    private List<Historico> buildInitialHistory(DispositivoVirtual dispositivo, int horasUsoEstimado) {
        return List.of(
                buildHistory(dispositivo, 3, horasUsoEstimado),
                buildHistory(dispositivo, 2, horasUsoEstimado + 1),
                buildHistory(dispositivo, 1, Math.max(1, horasUsoEstimado - 1))
        );
    }

    private Historico buildHistory(DispositivoVirtual dispositivo, int daysAgo, int horasUso) {
        int minutos = horasUso * 60;
        double kwh = (dispositivo.getPotenciaWatts() * horasUso) / 1000.0;

        return Historico.builder()
                .fechaRegistro(LocalDateTime.now().minusDays(daysAgo))
                .kwhConsumidos(kwh)
                .duracionMinutos(minutos)
                .dispositivo(dispositivo)
                .build();
    }

    private VerificacionEnviadaRespuestaDto simulateVerificationEmail(Usuario usuario) {
        String link = "/api/v1/auth/verify-email?token=" + usuario.getVerificationToken();
        return new VerificacionEnviadaRespuestaDto(
                usuario.getCorreo(),
                usuario.getVerificationToken(),
                usuario.getVerificationTokenExpiresAt(),
                link
        );
    }

    private void assignVerificationToken(Usuario usuario) {
        String token = jwtService.generateEmailVerificationToken(
                usuario.getCorreo(),
                VERIFICATION_TOKEN_EXPIRATION_MILLIS
        );
        usuario.setVerificationToken(token);
        usuario.setVerificationTokenExpiresAt(LocalDateTime.ofInstant(
                jwtService.extractExpiration(token).toInstant(),
                ZoneId.systemDefault()
        ));
    }

    private boolean isVerificationTokenValid(String token, Usuario usuario) {
        try {
            return jwtService.isEmailVerificationTokenValid(token, usuario.getCorreo());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private Rol findOrCreateRole(String roleName) {
        return rolRepositorio.findByNombre(roleName)
                .orElseGet(() -> rolRepositorio.save(Rol.builder()
                        .nombre(roleName)
                        .build()));
    }

    private ReniecRespuesta findPersonByDni(String dni) {
        try {
            ReniecRespuesta response = reniecClient.getPersonaInfo(dni, apiToken);
            if (response == null || response.getFirstName() == null || response.getFirstLastName() == null) {
                throw new BadRequestException("No se encontro informacion para el DNI brindado");
            }
            return response;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Error al consultar el servicio de Reniec");
        }
    }

    private String normalizeEmail(String correo) {
        return correo.trim().toLowerCase();
    }

    private String buildUsername(ReniecRespuesta response) {
        String firstName = response.getFirstName().split("\\s+")[0].toLowerCase();
        return firstName + "." + response.getFirstLastName().toLowerCase();
    }

    private String buildLastNames(ReniecRespuesta response) {
        String firstLastName = trimToNull(response.getFirstLastName());
        String secondLastName = trimToNull(response.getSecondLastName());

        if (secondLastName == null) {
            return firstLastName;
        }
        return firstLastName + " " + secondLastName;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
