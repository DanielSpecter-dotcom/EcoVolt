package com.ecovolt.demo.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "acciones_rutina")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccionRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", nullable = false)
    private Rutina rutina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id", nullable = false)
    private DispositivoVirtual dispositivo;

    @Column(name = "encender", nullable = false)
    private boolean encender;
}
