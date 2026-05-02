package com.ecovolt.demo.Service;

import com.ecovolt.demo.Dto.Request.LoginRequestDto;
import com.ecovolt.demo.Dto.Request.RegisterRequestDto;
import com.ecovolt.demo.Dto.Request.ResendVerificationRequestDto;
import com.ecovolt.demo.Dto.Request.VerifyEmailRequestDto;
import com.ecovolt.demo.Dto.Response.LoginResponseDto;
import com.ecovolt.demo.Dto.Response.ReniecResponse;
import com.ecovolt.demo.Dto.Response.VerificationSentResponseDto;
import com.ecovolt.demo.Entities.CasaEntity;
import com.ecovolt.demo.Entities.HabitacionEntity;
import com.ecovolt.demo.Entities.HistoricoEntity;
import com.ecovolt.demo.Entities.RolEntity;
import com.ecovolt.demo.Entities.UsuarioEntity;
import com.ecovolt.demo.Entities.VirtualDeviceEntity;
import com.ecovolt.demo.Enums.TipoUsuario;
import com.ecovolt.demo.Exception.BadRequestException;
import com.ecovolt.demo.Exception.ResourceNotFoundException;
import com.ecovolt.demo.Repository.CasaRepository;
import com.ecovolt.demo.Repository.HabitacionRepository;
import com.ecovolt.demo.Repository.HistoricoRepository;
import com.ecovolt.demo.Repository.RolRepository;
import com.ecovolt.demo.Repository.UsuarioRepository;
import com.ecovolt.demo.Repository.VirtualDeviceRepository;
import com.ecovolt.demo.Security.JwtService;
import com.ecovolt.demo.Service.FeingService.ReniecClient;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AuthService {

    private static final int TOKEN_EXPIRATION_HOURS = 24;
    private static final long VERIFICATION_TOKEN_EXPIRATION_MILLIS = TOKEN_EXPIRATION_HOURS * 60L * 60L * 1000L;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CasaRepository casaRepository;
    private final HabitacionRepository habitacionRepository;
    private final VirtualDeviceRepository virtualDeviceRepository;
    private final HistoricoRepository historicoRepository;
    private final ReniecClient reniecClient;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

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

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new BadRequestException("El correo ya se encuentra registrado");
        }

        if (usuarioRepository.existsByDni(dni)) {
            throw new BadRequestException("El DNI ya se encuentra registrado");
        }

        ReniecResponse reniecResponse = findPersonByDni(dni);

        UsuarioEntity usuario = modelMapper.map(request, UsuarioEntity.class);
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

        UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);

        if (usuarioGuardado.getTipoUsuario() == TipoUsuario.PERSONAL) {
            createDemoHome(usuarioGuardado);
        }

        return simulateVerificationEmail(usuarioGuardado);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequestDto request) {
        UsuarioEntity usuario = usuarioRepository.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("El token de verificacion no es valido"));

        if (!isVerificationTokenValid(request.getToken(), usuario)) {
            throw new BadRequestException("El token de verificacion ha expirado");
        }

        usuario.setActivo(true);
        usuario.setVerificationToken(null);
        usuario.setVerificationTokenExpiresAt(null);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public VerificationSentResponseDto resendVerification(ResendVerificationRequestDto request) {
        UsuarioEntity usuario = usuarioRepository.findByCorreo(normalizeEmail(request.getCorreo()))
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con el correo indicado"));

        if (usuario.isActivo()) {
            throw new BadRequestException("La cuenta ya se encuentra activa");
        }

        assignVerificationToken(usuario);
        UsuarioEntity usuarioGuardado = usuarioRepository.save(usuario);

        return simulateVerificationEmail(usuarioGuardado);
    }

    private void createDemoHome(UsuarioEntity usuario) {
        CasaEntity casa = casaRepository.save(CasaEntity.builder()
                .nombre("Hogar virtual de ejemplo")
                .usuario(usuario)
                .build());

        HabitacionEntity habitacion = habitacionRepository.save(HabitacionEntity.builder()
                .nombre("Sala principal")
                .casa(casa)
                .build());

        createDemoDevice(habitacion, "Luz LED", "luz", 10.0, 5);
        createDemoDevice(habitacion, "TV Smart", "TV", 120.0, 4);
        createDemoDevice(habitacion, "Refrigerador", "refrigerador", 150.0, 8);
    }

    private void createDemoDevice(HabitacionEntity habitacion, String nombre, String tipo, double watts, int horasUso) {
        VirtualDeviceEntity dispositivo = virtualDeviceRepository.save(VirtualDeviceEntity.builder()
                .nombre(nombre)
                .tipo(tipo)
                .potenciaWatts(watts)
                .activo(false)
                .automatico(false)
                .habitacion(habitacion)
                .build());

        historicoRepository.saveAll(buildInitialHistory(dispositivo, horasUso));
    }

    private List<HistoricoEntity> buildInitialHistory(VirtualDeviceEntity dispositivo, int horasUsoEstimado) {
        return List.of(
                buildHistory(dispositivo, 3, horasUsoEstimado),
                buildHistory(dispositivo, 2, horasUsoEstimado + 1),
                buildHistory(dispositivo, 1, Math.max(1, horasUsoEstimado - 1))
        );
    }

    private HistoricoEntity buildHistory(VirtualDeviceEntity dispositivo, int daysAgo, int horasUso) {
        int minutos = horasUso * 60;
        double kwh = (dispositivo.getPotenciaWatts() * horasUso) / 1000.0;

        return HistoricoEntity.builder()
                .fechaRegistro(LocalDateTime.now().minusDays(daysAgo))
                .kwhConsumidos(kwh)
                .duracionMinutos(minutos)
                .dispositivo(dispositivo)
                .build();
    }

    private VerificationSentResponseDto simulateVerificationEmail(UsuarioEntity usuario) {
        String link = "/api/v1/auth/verify-email?token=" + usuario.getVerificationToken();
        return new VerificationSentResponseDto(
                usuario.getCorreo(),
                usuario.getVerificationToken(),
                usuario.getVerificationTokenExpiresAt(),
                link
        );
    }

    private void assignVerificationToken(UsuarioEntity usuario) {
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

    private boolean isVerificationTokenValid(String token, UsuarioEntity usuario) {
        try {
            return jwtService.isEmailVerificationTokenValid(token, usuario.getCorreo());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private RolEntity findOrCreateRole(String roleName) {
        return rolRepository.findByNombre(roleName)
                .orElseGet(() -> rolRepository.save(RolEntity.builder()
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
