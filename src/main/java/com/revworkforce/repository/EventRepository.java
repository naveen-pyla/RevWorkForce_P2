package com.revworkforce.repository;

import com.revworkforce.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByEventDateGreaterThanEqualOrderByEventDateAsc(LocalDate date);
}
