package com.ecovolt.demo.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dispositivos_virtuales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualDeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipo; // Ej: "TV", "Refrigerador", "Luz"

    @Column(name = "potencia_watts", nullable = false)
    private Double potenciaWatts;

    @Column(name = "activo", nullable = false)
    private boolean activo = false;

    @Column(name = "automatico", nullable = false)
    private boolean automatico = false; // true = automático (rutinas), false = manual

    @Column(name = "limite_kwh")
    private Double limiteKwh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private HabitacionEntity habitacion;
}