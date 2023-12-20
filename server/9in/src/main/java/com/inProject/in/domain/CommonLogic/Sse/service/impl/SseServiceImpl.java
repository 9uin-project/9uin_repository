//sseServiceimple
package com.inProject.in.domain.CommonLogic.Sse.service.impl;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.config.security.CustomAccessDeniedHandler;
import com.inProject.in.config.security.JwtTokenProvider;
import com.inProject.in.domain.CommonLogic.Application.Dto.RequestApplicationDto;
import com.inProject.in.domain.CommonLogic.Sse.repository.SseRepository;
import com.inProject.in.domain.CommonLogic.Sse.service.SseService;
import com.inProject.in.domain.Notification.entity.Notification;
import com.inProject.in.domain.Notification.repository.NotificationRepository;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;


@Service
public class SseServiceImpl implements SseService {
    // 기본 타임아웃 설정
    private static final Long DEFAULT_TIMEOUT = 120L * 1000 * 60;
    private UserRepository userRepository;
    private JwtTokenProvider jwtTokenProvider;
    private  SseRepository sseRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    private NotificationRepository notificationRepository;

    private final Logger log = LoggerFactory.getLogger(SseServiceImpl.class);
    @Autowired
    SseServiceImpl(SseRepository sseRepository,
                   JwtTokenProvider jwtTokenProvider,
                   UserRepository userRepository,
                   ApplicationEventPublisher applicationEventPublisher,
                   NotificationRepository notificationRepository){
        this.userRepository = userRepository;
        this.sseRepository = sseRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.applicationEventPublisher = applicationEventPublisher;
        this.notificationRepository = notificationRepository;
    }
    @Override
    public SseEmitter subscribe(String username, String data) {

        SseEmitter emitter = sseRepository.get(username);
        sendToClient(username,data);

        return emitter;
    }
    @Override
    public void sendToClient(String username, String data) {
        SseEmitter emitter = sseRepository.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(username)
                        .name("notice")  //프론트에서 eventsource.addEventListener("sse" ...) 이 부분
                        .data(data));

                log.info("data 전송 완료 : message ==> " + data);
//                log.info("알림 저장 ==> 수신자 : " + savedNotification.getReceiver() + " 내용 : " + savedNotification.getMessage());
            } catch (IOException exception) {
                sseRepository.deleteById(username);
                emitter.completeWithError(exception);
            }
        }
        else if(emitter == null){
            log.info("sseEmitter is null");
        }
    }
    @Override
    public SseEmitter createEmitter(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        User user;

        if(token != null && jwtTokenProvider.validateToken(token)){
            String username = jwtTokenProvider.getUsername(token);
            user = userRepository.getByUsername(username)
                    .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.SSE, HttpStatus.NOT_FOUND, "sseCreate user를 찾지 못함"));
        }
        else{
            throw new CustomException(ConstantsClass.ExceptionClass.SSE, HttpStatus.UNAUTHORIZED, "token이 없거나, 권한이 유효하지 않습니다.");
        }

        String username =  user.getUsername();
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        //더미 데이터 send
        String dummy = "connect request";
        sseRepository.save(username, emitter);
        try {
            emitter.send(SseEmitter.event()
                    .id(username)
                    .name("notice")
                    .data(dummy));

            log.info("data 전송 완료 : message ==> " + dummy);
        } catch (IOException exception) {
            sseRepository.deleteById(username);
            emitter.completeWithError(exception);
        }

//        // Emitter가 완료될 때(모든 데이터가 성공적으로 전송된 상태) Emitter를 삭제한다.한번 보내고 말것이 아니라 주석처리 하였음.
        emitter.onCompletion(() -> sseRepository.deleteById(username));

        // Emitter가 타임아웃 되었을 때(지정된 시간동안 어떠한 이벤트도 전송되지 않았을 때) Emitter를 삭제한다.
        emitter.onTimeout(() -> sseRepository.deleteById(username));
        emitter.onError((e) -> sseRepository.deleteById(username));
        return emitter;
    }

    //로그아웃 시 호출
    @Override
    public void closeEmitter(String username){
        sseRepository.deleteById(username);
    }
}