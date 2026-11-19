package com.eldercare.handover.repo;

import com.eldercare.handover.model.MoodEntry;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {

    List<MoodEntry> findByRecordedAtGreaterThanEqualOrderByRecordedAtAsc(LocalDateTime since);
}
