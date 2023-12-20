package com.inProject.in.domain.Notification.repository.Impl;

import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.entity.QApplicantBoardRelation;
import com.inProject.in.domain.Notification.entity.Notification;
import com.inProject.in.domain.Notification.entity.QNotification;
import com.inProject.in.domain.Notification.repository.CustomNotificationRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {
    private final JPAQueryFactory jpaQueryFactory;
    QNotification qNotification = QNotification.notification;

    @Override
    public Optional<List<Notification>> getByUserIdAndIsCheck(Long user_id) {

        List<Notification> query = jpaQueryFactory
                .selectFrom(qNotification)
                .where(qNotification.receiver.id.eq(user_id), qNotification.isChecked.eq(true))
                .orderBy(qNotification.id.desc())
                .fetch();

        return Optional.ofNullable(query);
    }
}
