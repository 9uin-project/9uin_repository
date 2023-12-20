package com.inProject.in.domain.Notification.service;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.Board.service.impl.BoardServiceImpl;
import com.inProject.in.domain.Notification.Dto.request.RequestNotificationDto;
import com.inProject.in.domain.Notification.Dto.response.ResponseNotificationDto;
import com.inProject.in.domain.Notification.entity.Notification;
import com.inProject.in.domain.Notification.repository.NotificationRepository;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final Logger log = LoggerFactory.getLogger(BoardServiceImpl.class);

    @Transactional
    public List<ResponseNotificationDto> getNotificationList(Long user_id){

        List<Notification> notificationList = notificationRepository.getByUserIdAndIsCheck(user_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.NOTIFICATION, HttpStatus.NOT_FOUND,  user_id + " 은 없는 유저입니다."));

        List<ResponseNotificationDto> responseNotificationDtoList = new ArrayList<>();

        for(Notification notification : notificationList){
            ResponseNotificationDto responseNotificationDto = new ResponseNotificationDto(notification);
            responseNotificationDtoList.add(responseNotificationDto);
        }

        return responseNotificationDtoList;
    }
    @Transactional
    public ResponseNotificationDto createNotification(RequestNotificationDto requestNotificationDto){
        String receiverName = requestNotificationDto.getReceiverName();
        String senderName = requestNotificationDto.getSenderName();
        Long board_id = requestNotificationDto.getBoard_id();

        User receiver = userRepository.getByUsername(receiverName)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.NOTIFICATION, HttpStatus.NOT_FOUND, receiverName + " 는 없는 유저입니다."));

        User sender = userRepository.getByUsername(senderName)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.NOTIFICATION, HttpStatus.NOT_FOUND, senderName + " 는 없는 유저입니다."));

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, board_id + " 게시글은 존재하지 않습니다."));

        Notification savedNotification = notificationRepository.save(requestNotificationDto.toEntity(receiver, sender, board));
        log.info("알림 저장 완료 ==> " + savedNotification.getReceiver().getUsername() + " 에게 알림 동작");
        log.info("내용 ==> " + savedNotification.getMessage());

        ResponseNotificationDto responseNotificationDto = new ResponseNotificationDto(savedNotification);
        return responseNotificationDto;
    }
    @Transactional
    public ResponseNotificationDto updateToCheckNotification(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.NOTIFICATION, HttpStatus.NOT_FOUND, notification_id + " 를 찾을 수 없습니다."));


        notification.setChecked(false);
        Notification updatedNotification = notificationRepository.save(notification);

        ResponseNotificationDto responseNotificationDto = new ResponseNotificationDto(updatedNotification);

        return responseNotificationDto;
    }
    @Transactional
    public void deleteNotification(Long id){
        notificationRepository.deleteById(id);
        log.info("notification Service ==> " + id + " 알림 삭제");
    }

}
