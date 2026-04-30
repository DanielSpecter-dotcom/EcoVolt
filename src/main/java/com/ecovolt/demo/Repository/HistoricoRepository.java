package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.HistoricoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoRepository extends JpaRepository<HistoricoEntity, Long> {
}
