package com.ecovolt.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
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

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id", nullable = false)
    private Casa casa;

    @Column(name = "execution_time", nullable = false)
    private LocalTime executionTime;

    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "rutinas_dias_semana", joinColumns = @JoinColumn(name = "rutina_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private Set<DayOfWeek> daysOfWeek;

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccionRutina> acciones;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "paused_by_away_mode", nullable = false)
    private boolean pausedByAwayMode = false;
}
