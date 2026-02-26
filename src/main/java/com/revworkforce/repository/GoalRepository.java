package com.revworkforce.repository;

import com.revworkforce.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByEmployee_EmpId(Long empId);
}