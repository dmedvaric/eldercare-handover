package com.eldercare.handover.repo;

import com.eldercare.handover.model.Resident;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentRepository extends JpaRepository<Resident, Long> {

    List<Resident> findAllByOrderByRoomAscBedAsc();
}
