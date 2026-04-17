package com.ecovolt.demo.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "rutinas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RutinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Column(name = "dias_semana")
    private String diasSemana; // Ej: "1,2,3,4,5" para Lunes a Viernes

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "pausado_ausente", nullable = false)
    private boolean pausadoAusente = false; // Para el modo "Ausente"

    @ManyToMany
    @JoinTable(
            name = "rutinas_dispositivos",
            joinColumns = @JoinColumn(name = "rutina_id"),
            inverseJoinColumns = @JoinColumn(name = "dispositivo_id")
    )
    private List<VirtualDeviceEntity> dispositivos;
}