package com.revworkforce.repository;

import com.revworkforce.entity.Events;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventsRepository extends JpaRepository<Events, Long> {
}