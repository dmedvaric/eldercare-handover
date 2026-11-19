package com.eldercare.handover.repo;

import com.eldercare.handover.model.Medication;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    @Query("select m from Medication m where m.onlyOnDate is null or m.onlyOnDate = :today order by m.scheduledTime, m.id")
    List<Medication> findActiveOn(@Param("today") LocalDate today);
}
