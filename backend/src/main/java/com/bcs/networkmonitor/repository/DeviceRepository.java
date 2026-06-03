package com.bcs.networkmonitor.repository;

import com.bcs.networkmonitor.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUniqueId(String uniqueId);

    boolean existsByUniqueId(String uniqueId);

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.statusReports sr WHERE d.id = :id ORDER BY sr.reportedAt DESC")
    Optional<Device> findByIdWithReports(@Param("id") Long id);
}
