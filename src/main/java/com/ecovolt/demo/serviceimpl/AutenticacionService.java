package com.ecovolt.demo.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;

import com.ecovolt.demo.dtos.request.LoginRequestDto;
import com.ecovolt.demo.dtos.request.RegisterRequestDto;
import com.ecovolt.demo.dtos.request.ResendVerificationRequestDto;
import com.ecovolt.demo.dtos.request.VerifyEmailRequestDto;
import com.ecovolt.demo.dtos.response.LoginResponseDto;
import com.ecovolt.demo.dtos.response.ReniecResponse;
import com.ecovolt.demo.dtos.response.VerificationSentResponseDto;
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
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private RolRepositorio rolRepositorio;
    @Autowired
    private CasaRepositorio casaRepositorio;
    @Autowired
    private HabitacionRepositorio habitacionRepositorio;
    @Autowired
    private DispositivoVirtualRepositorio dispositivoVirtualRepositorio;
    @Autowired
    private HistoricoRepositorio historicoRepositorio;
    @Autowired
    private ReniecClient reniecClient;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ModelMapper modelMapper;

    @Value("${api.token}")
    private String apiToken;

    public LoginResponseDto login(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizeEmail(request.getCorreo()),
                        request.getContrasena()
                )
        );
        String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());
        return new LoginResponseDto(token, "Bearer", jwtService.getExpirationSeconds());
    }

    @Transactional
    public VerificationSentResponseDto register(RegisterRequestDto request) {
        String correo = normalizeEmail(request.getCorreo());
        String dni = request.getDni().trim();

        if (usuarioRepositorio.existsByCorreo(correo)) {
            throw new BadRequestException("El correo ya se encuentra registrado");
        }

        if (usuarioRepositorio.existsByDni(dni)) {
            throw new BadRequestException("El DNI ya se encuentra registrado");
        }

        ReniecResponse reniecResponse = findPersonByDni(dni);

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
    public void verifyEmail(VerifyEmailRequestDto request) {
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
    public VerificationSentResponseDto resendVerification(ResendVerificationRequestDto request) {
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

    private VerificationSentResponseDto simulateVerificationEmail(Usuario usuario) {
        String link = "/api/v1/auth/verify-email?token=" + usuario.getVerificationToken();
        return new VerificationSentResponseDto(
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

    private ReniecResponse findPersonByDni(String dni) {
        try {
            ReniecResponse response = reniecClient.getPersonaInfo(dni, apiToken);
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

    private String buildUsername(ReniecResponse response) {
        String firstName = response.getFirstName().split("\\s+")[0].toLowerCase();
        return firstName + "." + response.getFirstLastName().toLowerCase();
    }

    private String buildLastNames(ReniecResponse response) {
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
