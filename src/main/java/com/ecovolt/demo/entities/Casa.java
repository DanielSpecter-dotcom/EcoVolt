package com.ecovolt.demo.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "casas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Casa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Ej: "Casa de Playa", "Sucursal Miraflores"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // "mappedBy" apunta a la variable 'casa' en Habitacion
    @OneToMany(mappedBy = "casa", cascade = CascadeType.ALL)
    private List<Habitacion> habitaciones;

    //holaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa :P
    //Como tas Copil?
    //Mal borra tu word porfa :((((((((((((
    //xd
    //habla a la siguiente ponemos origin main tu diras
}