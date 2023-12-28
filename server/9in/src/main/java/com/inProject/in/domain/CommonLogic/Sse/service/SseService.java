//sseService
package com.inProject.in.domain.CommonLogic.Sse.service;

import com.inProject.in.domain.CommonLogic.Application.Dto.RequestApplicationDto;
import com.inProject.in.domain.Notification.Dto.request.RequestNotificationDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface SseService {
    SseEmitter subscribe(String user_id, Object data);
    void sendToClient(String username, Object data);
    SseEmitter createEmitter(HttpServletRequest request);
    void closeEmitter(String username);
}
