package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<RolEntity, Long> {

    Optional<RolEntity> findByNombre(String nombre);
}
