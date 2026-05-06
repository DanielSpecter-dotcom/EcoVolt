package com.ecovolt.demo.entities;

import com.ecovolt.demo.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String dni;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String apellido;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String contrasena;

    @Column(name = "tipo_usuario", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoUsuario tipoUsuario;

    @Column(name = "nombre_empresa")
    private String nombreEmpresa;

    @Column(name = "ruc")
    private String ruc;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean activo = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;

    @Column(name = "notificar_consumo_excesivo", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean notificarConsumoExcesivo = true;

    @Column(name = "notificar_uso_prolongado", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean notificarUsoProlongado = true;

    @Column(name = "notificar_reporte_semanal", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean notificarReporteSemanal = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuarios_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "rol_id"})
    )
    @Builder.Default
    private Set<Rol> roles = new HashSet<>();
}
