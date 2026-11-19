package com.eldercare.handover.repo;

import com.eldercare.handover.model.MedicationAdministration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, Long> {

    List<MedicationAdministration> findByAdministeredOn(LocalDate date);

    Optional<MedicationAdministration> findByMedicationIdAndAdministeredOn(Long medicationId, LocalDate date);
}
