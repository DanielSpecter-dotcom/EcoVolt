package com.ecovolt.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_registros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "kwh_consumidos", nullable = false)
    private Double kwhConsumidos;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private DispositivoVirtual dispositivo;
}