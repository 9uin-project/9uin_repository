package com.inProject.in.domain.Notification.repository;

import com.inProject.in.domain.Notification.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface CustomNotificationRepository {
    Optional<List<Notification>> getByUserIdAndIsCheck(Long user_id);
}
