package com.inProject.in.domain.Notification.controller;

import com.inProject.in.domain.Board.Dto.response.ResponseBoardListDto;
import com.inProject.in.domain.Notification.Dto.request.RequestNotificationDto;
import com.inProject.in.domain.Notification.Dto.response.ResponseNotificationDto;
import com.inProject.in.domain.Notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notify")
@Tag(name = "notification", description = "알림 관련 api")
public class NotificationController {
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @GetMapping()
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰", in = ParameterIn.HEADER)
    @Parameter(name = "user_id", description = "알림 조회하는 유저의 ID", in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int64"))
    @Operation(summary ="사용자의 알림 리스트 조회", description = "확인하지 않은 알림들을 가져옵니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 리스트 조회 성공", content = {
                            @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseNotificationDto.class))
                    })
            })
    public ResponseEntity<List<ResponseNotificationDto>> getNotifications(@RequestParam(name = "user_id") Long user_id){
        List<ResponseNotificationDto> responseNotificationDtoList = notificationService.getNotificationList(user_id);

        return ResponseEntity.status(HttpStatus.OK).body(responseNotificationDtoList);
    }

    @PostMapping()
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰", in = ParameterIn.HEADER)
    @Operation(summary ="대상에게 알림 생성", description = "알림이 발생할 때 호출됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 생성 성공", content = {
                            @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseNotificationDto.class))
                    })
            })
    public ResponseEntity<ResponseNotificationDto> createNotification(@RequestBody RequestNotificationDto requestNotificationDto){
        ResponseNotificationDto responseNotificationDto = notificationService.createNotification(requestNotificationDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseNotificationDto);
    }


    @PutMapping()
    @Parameter(name = "notification_id", description = "확인한 알림의 id", in = ParameterIn.QUERY, schema = @Schema(type = "integer", format = "int64"))
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰", in = ParameterIn.HEADER)
    @Operation(summary = "알림 확인하기", description = "읽지 않은 알림을 확인으로 체크합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 확인 성공", content = {
                            @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseNotificationDto.class))
                    })
            })
    public ResponseEntity<ResponseNotificationDto> updateNotification(@RequestParam(name = "notification_id") Long notification_id){
        ResponseNotificationDto responseNotificationDto = notificationService.updateToCheckNotification(notification_id);

        return ResponseEntity.status(HttpStatus.OK).body(responseNotificationDto);
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteNotification(@RequestParam Long notification_id){
        notificationService.deleteNotification(notification_id);

        return ResponseEntity.status(HttpStatus.OK).body(notification_id + " 삭제 완료");
    }
}
