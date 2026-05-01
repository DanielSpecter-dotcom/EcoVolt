package com.ecovolt.demo.Repository;

import com.ecovolt.demo.Entities.VirtualDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VirtualDeviceRepository extends JpaRepository<VirtualDeviceEntity, Long> {
}
