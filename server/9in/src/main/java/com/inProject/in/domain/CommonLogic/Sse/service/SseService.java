//sseService
package com.inProject.in.domain.CommonLogic.Sse.service;

import com.inProject.in.domain.CommonLogic.Application.Dto.RequestApplicationDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface SseService {
    SseEmitter subscribe(String user_id, String data);
    void sendToClient(String username, String data);
    SseEmitter createEmitter(HttpServletRequest request);
    void closeEmitter(String username);
}
