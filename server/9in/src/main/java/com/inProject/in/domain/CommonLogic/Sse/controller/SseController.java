package com.inProject.in.domain.CommonLogic.Sse.controller;




import com.inProject.in.domain.CommonLogic.Application.Dto.request.RequestApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.response.ResponseSseDto;
import com.inProject.in.domain.CommonLogic.Application.service.ApplicationService;
import com.inProject.in.domain.CommonLogic.Sse.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class SseController {

    private final SseService sseService;
    private final ApplicationService applicationService;
    private SseController(SseService sseService,
                          ApplicationService applicationService){

        this.sseService = sseService;
        this.applicationService = applicationService;
    }


    //로그인성공시 반드시 sseConnect를 호출해야 합니다.
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)  //produces = MediaType.TEXT_EVENT_STREAM_VALUE
    public SseEmitter SseConnect(HttpServletRequest request){
        SseEmitter sse = sseService.createEmitter(request);
        return sse;
    }


    // id 에게 알림메세지를 보낸다.
    @GetMapping(value = "/apply") //반드시 json으로 반환 produces = MediaType.APPLICATION_JSON_VALUE//
    @Operation(summary = "지원하기 알림 보내기", description = "게시글에 지원한 후, 작성자에게 알림을 보냅니다.")
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    public void subscribe(@RequestBody RequestApplicationDto requestApplicationDto) {
//        Long board_id_long = Long.parseLong(id);

        ResponseSseDto responseSseDto = applicationService.ApplicationToSseResponse(requestApplicationDto.getBoard_id(), requestApplicationDto.getRole_id());

        String message = responseSseDto.getTitle()+" 의 "+ responseSseDto.getRole() +" 에 신청이 1건 있습니다.";
        SseEmitter sseEmitter = sseService.subscribe(requestApplicationDto.getAuthorName(), message);
    };
}
