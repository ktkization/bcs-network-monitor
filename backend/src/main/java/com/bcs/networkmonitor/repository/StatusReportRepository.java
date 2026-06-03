package com.bcs.networkmonitor.repository;

import com.bcs.networkmonitor.entity.StatusReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusReportRepository extends JpaRepository<StatusReport, Long> {
    List<StatusReport> findTop20ByDeviceIdOrderByReportedAtDesc(Long deviceId);

    @Query("SELECT sr FROM StatusReport sr WHERE sr.device.id = :deviceId ORDER BY sr.reportedAt DESC")
    List<StatusReport> findRecentByDeviceId(@Param("deviceId") Long deviceId, Pageable pageable);

    Optional<StatusReport> findTopByDeviceIdOrderByReportedAtDesc(Long deviceId);
}
