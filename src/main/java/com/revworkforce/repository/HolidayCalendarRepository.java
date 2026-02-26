package com.revworkforce.repository;

import com.revworkforce.entity.HolidayCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, Long> {
}