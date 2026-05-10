package com.ecovolt.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "dispositivos_virtuales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispositivoVirtual {

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

    @Column(name = "eliminado", nullable = false)
    private boolean eliminado = false;

    @Column(name = "limite_kwh")
    private Double limiteKwh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

    @OneToMany(mappedBy = "dispositivo", cascade = CascadeType.ALL)
    private List<Historico> historialRegistros;

    @OneToMany(mappedBy = "dispositivo", cascade = CascadeType.ALL)
    private List<Alerta> alertas;
}
