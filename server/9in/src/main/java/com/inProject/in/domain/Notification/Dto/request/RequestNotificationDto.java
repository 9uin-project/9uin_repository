package com.inProject.in.domain.Notification.Dto.request;

import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Notification.entity.Notification;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import com.inProject.in.domain.User.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestNotificationDto {
    private String receiverName;
    private String senderName;
    private Long board_id;
    private Long role_id;
    private String message;
    private String alarm_type;
    private boolean isChecked;   //알림 읽었는지 여부.


    public Notification toEntity(User receiver, User sender, Board board, RoleNeeded roleNeeded) {
        return Notification.builder()
                .alarm_type(alarm_type)
                .isChecked(isChecked)
                .message(message)
                .receiver(receiver)
                .sender(sender)
                .board(board)
                .roleNeeded(roleNeeded)
                .build();
    }
}
