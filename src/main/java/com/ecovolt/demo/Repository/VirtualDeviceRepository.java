package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.VirtualDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VirtualDeviceRepository extends JpaRepository<VirtualDeviceEntity, Long> {

    Optional<VirtualDeviceEntity> findByIdAndHabitacionCasaUsuarioId(Long id, Long usuarioId);

    List<VirtualDeviceEntity> findByHabitacionCasaUsuarioId(Long usuarioId);

    Optional<VirtualDeviceEntity> findByIdAndEliminadoFalse(Long id);

    List<VirtualDeviceEntity> findByEliminadoFalseOrderByIdAsc();
}
