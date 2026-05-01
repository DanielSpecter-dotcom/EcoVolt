package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.CasaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CasaRepository extends JpaRepository<CasaEntity, Long> {

    Optional<CasaEntity> findFirstByUsuarioIdOrderByIdAsc(Long usuarioId);
}
