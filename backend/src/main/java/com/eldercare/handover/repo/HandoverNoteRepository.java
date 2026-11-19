package com.eldercare.handover.repo;

import com.eldercare.handover.model.HandoverNote;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HandoverNoteRepository extends JpaRepository<HandoverNote, Long> {

    @Query("select n from HandoverNote n where n.createdAt >= :since or n.acknowledgedAt is null order by n.createdAt desc")
    List<HandoverNote> findVisible(@Param("since") LocalDateTime since);
}
