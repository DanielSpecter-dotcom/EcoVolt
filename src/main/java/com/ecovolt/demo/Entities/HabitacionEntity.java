package com.ecovolt.demo.Entities;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "habitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id", nullable = false)
    private CasaEntity casa;

    // "mappedBy" apunta a la variable 'habitacion' en VirtualDeviceEntity
    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL)
    private List<VirtualDeviceEntity> dispositivos;
}