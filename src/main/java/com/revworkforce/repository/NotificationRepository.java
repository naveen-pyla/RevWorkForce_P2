package com.revworkforce.repository;

import com.revworkforce.entity.Employee;
import com.revworkforce.entity.Notification;
import com.revworkforce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_UserIdAndIsReadFalse(Long userId);

    void deleteByUser(User user);
}