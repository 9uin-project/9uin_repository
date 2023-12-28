package com.inProject.in.domain.Notification.Dto.response;

import com.inProject.in.domain.Notification.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseNotificationDto {
    private Long id;
    private String receiverName;
    private String senderName;
    private Long board_id;
    private String boardName;
    private String board_type;
    private Long role_id;
    private String roleName;
    private String message;
    private String alarm_type;
    private LocalDateTime createAt;

    public ResponseNotificationDto(Notification notification){
        this.id = notification.getId();
        this.receiverName = notification.getReceiver().getUsername();
        this.senderName = notification.getSender().getUsername();
        this.board_id = notification.getBoard().getId();
        this.boardName = notification.getBoard().getTitle();
        this.board_type = notification.getBoard().getType();
        this.role_id = notification.getRoleNeeded().getId();
        this.roleName = notification.getRoleNeeded().getName();
        this.message = notification.getMessage();
        this.alarm_type = notification.getAlarm_type();
        this.createAt = notification.getCreateAt();
    }

}
