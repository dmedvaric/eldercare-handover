package com.eldercare.handover.repo;

import com.eldercare.handover.model.ActivityEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, Long> {

    List<ActivityEvent> findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(LocalDateTime since);
}
