package com.inProject.in.domain.CommonLogic.Application.controller;

import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.domain.Board.service.BoardService;
import com.inProject.in.domain.CommonLogic.Application.Dto.ResponseApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.RequestApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.ResponseSseDto;
import com.inProject.in.domain.CommonLogic.Application.service.ApplicationService;
import com.inProject.in.domain.CommonLogic.Sse.service.SseService;
import com.inProject.in.domain.Notification.Dto.request.RequestNotificationDto;
import com.inProject.in.domain.Notification.Dto.response.ResponseNotificationDto;
import com.inProject.in.domain.Notification.service.NotificationService;
import com.inProject.in.domain.User.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "application", description = "게시글에 지원하는 api")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final SseService sseService;
    private final BoardService boardService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @PostMapping()
    @Operation(summary = "지원하기", description = "게시글에 지원합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "지원 조회 성공", content = {
                            @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseApplicationDto.class))
                    })
            })
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    public ResponseEntity<ResponseApplicationDto> createApplication(@RequestBody RequestApplicationDto requestApplicationDto, HttpServletRequest request){

        try{
            ResponseApplicationDto responseApplicationDto = applicationService.createApplication(requestApplicationDto, request);
            //sseEvent 게시자의 id 로 바꿔야됨.
            Long board_id = requestApplicationDto.getBoard_id();

            ResponseSseDto responseSseDto = applicationService.ApplicationToSseResponse(requestApplicationDto);
            String message = responseSseDto.getTitle()+" 의 "+ responseSseDto.getRole() +" 에 신청이 1건 있습니다.";

            RequestNotificationDto requestNotificationDto = RequestNotificationDto.builder()
                    .alarm_type("message")
                    .board_id(board_id)
                    .receiverName(requestApplicationDto.getAuthorName())
                    .isChecked(false)
                    .message(message)
                    .build();

            sseService.subscribe(requestApplicationDto.getAuthorName(), message);

            return ResponseEntity.status(HttpStatus.OK).body(responseApplicationDto);
        }catch (CustomException e){
            throw e;
        }
    }

    @DeleteMapping()
    @Operation(summary = "지원 취소", description = "게시글에 지원한 걸 취소합니다.")
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    public ResponseEntity<String> deleteApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request){
        try{
            applicationService.deleteApplication(requestApplicationDto, request);

            return ResponseEntity.status(HttpStatus.OK).body("삭제 완료");
        }catch (CustomException e){
            throw e;
        }
    }

    @PostMapping("reject")
    @Operation(summary = "지원 거절", description = "게시글에 지원한 걸 거절합니다.")
    public ResponseEntity<String> rejectApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request){
        try{
            applicationService.rejectApplication(requestApplicationDto, request);
            ResponseSseDto responseSseDto = applicationService.ApplicationToSseResponse(requestApplicationDto);
            String message = "지원하신" + responseSseDto.getTitle()+"지원글에 참가하지 못하셨습니다.";
            sseService.subscribe(requestApplicationDto.getAuthorName(),message);
            return ResponseEntity.status(HttpStatus.OK).body("거절완료");
        }catch (CustomException e){
            throw e;
        }
    }

    @PostMapping("accept")
    @Operation(summary = "지원 수락", description = "게시글에 지원한 걸 수락합니다.")
    public ResponseEntity<String> acceptApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request){
        try{
            applicationService.acceptApplication(requestApplicationDto, request);
            //~
            ResponseSseDto responseSseDto = applicationService.ApplicationToSseResponse(requestApplicationDto);
            String message = "지원하신" + responseSseDto.getTitle()+"의 팀에 참가하게 되었습니다.";
            sseService.subscribe(requestApplicationDto.getAuthorName(),message);
            return ResponseEntity.status(HttpStatus.OK).body("수락완료");
        }catch (CustomException e){
            throw e;
        }
    }

}