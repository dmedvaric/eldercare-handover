package com.eldercare.handover.repo;

import com.eldercare.handover.model.CareTask;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CareTaskRepository extends JpaRepository<CareTask, Long> {


    @Query("select t from CareTask t where t.onlyOnDate = :today "
            + "or (t.onlyOnDate is null and (t.done = false or t.doneAt >= :startOfDay)) order by t.id")
    List<CareTask> findVisible(@Param("today") LocalDate today, @Param("startOfDay") LocalDateTime startOfDay);
}
