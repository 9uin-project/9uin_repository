package com.inProject.in.domain.CommonLogic.Sign.controller;

import com.inProject.in.domain.CommonLogic.Sign.Dto.request.*;
import com.inProject.in.domain.CommonLogic.Sign.Dto.response.*;
import com.inProject.in.domain.CommonLogic.Sign.service.SignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sign")
@Tag(name = "sign", description = "로그인, 회원가입 api")
public class SignController {
    private final Logger log = LoggerFactory.getLogger(SignController.class);
    private SignService signService;

    @Autowired
    public SignController(SignService signService){
        this.signService = signService;
    }

    @PostMapping("/sign-in")
    @Operation(summary = "로그인 시도", description = "생성되어있는 계정으로 로그인합니다.")
    public ResponseEntity<ResponseSignInDto> signIn(@RequestBody RequestSignInDto requestSignInDto) throws RuntimeException{
        log.info("SignController signIn ==> 로그인 시도   id : " + requestSignInDto.getUsername());

        ResponseSignInDto responseSignInDto = signService.signIn(requestSignInDto);

        if(responseSignInDto.getCode() == 0){
            log.info("로그인 성공 ==> token : " + responseSignInDto.getToken());
        }

        return ResponseEntity.status(HttpStatus.OK).body(responseSignInDto);
    }

    @PostMapping("/sign-up")
    @Operation(summary = "회원가입", description = "회원가입을 시도합니다.")
    public ResponseEntity<ResponseSignUpDto> signUp(@RequestBody RequestSignUpDto requestSignUpDto){
        log.info("SignController signUp ==> 회원가입 시도   id : " + requestSignUpDto.getUsername() + " mail : " + requestSignUpDto.getMail() +
                " role : " + requestSignUpDto.getRole());
        ResponseSignUpDto responseSignUpDto = signService.signUp(requestSignUpDto);

        log.info("회원가입 완료");
        return ResponseEntity.status(HttpStatus.OK).body(responseSignUpDto);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ResponseRefreshDto> reissue(@RequestBody RequestRefreshDto requestRefreshDto, HttpServletRequest request){
        log.info("SignController reissue ==> 토큰 재발급 메서드");
        ResponseRefreshDto responseRefreshDto = signService.reissue(requestRefreshDto, request);

        return ResponseEntity.status(HttpStatus.OK).body(responseRefreshDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RequestLogoutDto requestLogoutDto){
        log.info("SignController logout ==> 로그아웃 시작");
        signService.logout(requestLogoutDto);
        return ResponseEntity.ok("로그아웃 완료");
    }

    @GetMapping("/exception")
    public void exception() throws RuntimeException{
        throw new RuntimeException("접근이 금지되었습니다.");
    }



//    @ExceptionHandler(value = RuntimeException.class)
//    public ResponseEntity<Map<String, String>> ExceptionHandler(RuntimeException e){
//        HttpHeaders responseHeaders = new HttpHeaders();
//        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
//
//        log.info("ExceptionHandler ==> " + e.getCause() + " " + e.getMessage());
//
//        Map<String, String> m = new HashMap<>();
//
//        m.put("error type", httpStatus.getReasonPhrase());
//        m.put("code", "400");
//        m.put("message", "에러 발생");
//
//        return new ResponseEntity<>(m, responseHeaders, httpStatus);
//    }
}
