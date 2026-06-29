package com.ecovolt.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "rutinas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id", nullable = false)
    private Casa casa;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "tiempo_ejecucion", nullable = false)
    private LocalTime tiempoEjecucion;

    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "rutina_dias", joinColumns = @JoinColumn(name = "rutina_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana")
    private Set<DayOfWeek> diasSemana;

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<AccionRutina> acciones = new ArrayList<>();

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "pausado_ausente", nullable = false)
    @Builder.Default
    private boolean pausadoAusente = false;
}