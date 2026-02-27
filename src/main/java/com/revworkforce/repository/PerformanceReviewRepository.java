package com.revworkforce.repository;

import com.revworkforce.entity.Employee;
import com.revworkforce.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    List<PerformanceReview> findByEmployee_EmpIdOrderByYearDesc(Long empId);

    void deleteByEmployee(Employee employee);
}
