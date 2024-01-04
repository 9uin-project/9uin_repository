package com.inProject.in.domain.CommonLogic.Application.controller;

import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.domain.Board.service.BoardService;
import com.inProject.in.domain.CommonLogic.Application.Dto.request.RequestAcceptDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.response.ResponseApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.request.RequestApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.response.ResponseSseDto;
import com.inProject.in.domain.CommonLogic.Application.service.ApplicationService;
import com.inProject.in.domain.CommonLogic.Sse.service.SseService;
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

            ResponseSseDto responseSseDto = applicationService.ApplicationToSseResponse(requestApplicationDto.getBoard_id(), requestApplicationDto.getRole_id());
            String message = responseSseDto.getTitle()+" 의 "+ responseSseDto.getRole() +" 에 신청이 1건 있습니다.";

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

    @PostMapping("/reject")
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    @Operation(summary = "지원 거절", description = "게시글에 지원한 걸 거절합니다.")
    public ResponseEntity<String> rejectApplication(@RequestBody RequestAcceptDto requestAcceptDto, HttpServletRequest request){
        try{
            applicationService.rejectApplication(requestAcceptDto, request);
            ResponseSseDto responseSseDto = applicationService.ApplicationToSseResponse(requestAcceptDto.getBoard_id(), requestAcceptDto.getRole_id());
            String message = "지원하신" + responseSseDto.getTitle()+"지원글에 참가하지 못하셨습니다.";
            sseService.subscribe(requestAcceptDto.getSenderName(),message);
            return ResponseEntity.status(HttpStatus.OK).body("거절완료");
        }catch (CustomException e){
            throw e;
        }
    }

    @PostMapping("/accept")
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    @Operation(summary = "지원 수락", description = "게시글에 지원한 걸 수락합니다.")
    public ResponseEntity<String> acceptApplication(@RequestBody RequestAcceptDto requestAcceptDto, HttpServletRequest request){
        try{
            applicationService.acceptApplication(requestAcceptDto, request);
            ResponseSseDto responseSseDto = applicationService.ApplicationToSseResponse(requestAcceptDto.getBoard_id(), requestAcceptDto.getRole_id());
            String message = "지원하신" + responseSseDto.getTitle()+"의 팀에 참가하게 되었습니다.";
            sseService.subscribe(requestAcceptDto.getSenderName(),message);
            return ResponseEntity.status(HttpStatus.OK).body("수락완료");
        }catch (CustomException e){
            throw e;
        }
    }

}