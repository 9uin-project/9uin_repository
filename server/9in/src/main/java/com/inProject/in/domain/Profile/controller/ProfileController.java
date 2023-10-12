package com.inProject.in.domain.Profile.controller;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.domain.Profile.Dto.request.*;
import com.inProject.in.domain.Profile.Dto.response.*;
import com.inProject.in.domain.Profile.service.*;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private CertificateServiceImpl certificateService;
    private Job_exServiceImpl jobExService;
    private EducationServiceImpl educationService;
    private Project_skillServiceImpl projectSkillService;
    private MyInfoServiceImpl myInfoService;
    private UserRepository userRepository;

    @Autowired
    public ProfileController(
            CertificateServiceImpl certificateService,
            Job_exServiceImpl jobExService,
            EducationServiceImpl educationService,
            Project_skillServiceImpl projectSkillService,
            MyInfoServiceImpl myInfoService,
            UserRepository userRepository){

        this.certificateService = certificateService;
        this.educationService = educationService;
        this.jobExService = jobExService;
        this.projectSkillService = projectSkillService;
        this.myInfoService = myInfoService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{username}")
    @Parameter(name = "username", description = "username 입력", in = ParameterIn.PATH)
    public ResponseEntity<ResponseProfileDto> getProfile(@PathVariable(name = "username") String username){

        User user = userRepository.getByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("ProfileController getProfile에서 잘못된 username : " + username));

        if(user.getCertificate() == null || user.getEducation() == null || user.getJobEx() == null || user.getProjectSkill() == null || user.getMyInfo() == null){
            throw new CustomException(ConstantsClass.ExceptionClass.PROFILE, HttpStatus.BAD_REQUEST, username + "의 프로필작성이 완료되지 않았습니다.");
        }

        ResponseCertificateDto responseCertificateDto = new ResponseCertificateDto(user.getCertificate());
        ResponseJob_exDto responseJobExDto = new ResponseJob_exDto(user.getJobEx());
        ResponseEducationDto responseEducationDto = new ResponseEducationDto(user.getEducation());
        ResponseProject_skillDto responseProjectSkillDto = new ResponseProject_skillDto(user.getProjectSkill());
        ResponseMyInfoDto responseMyInfoDto = new ResponseMyInfoDto(user.getMyInfo());

        ResponseProfileDto responseProfileDto = new ResponseProfileDto(responseCertificateDto, responseProjectSkillDto, responseJobExDto, responseEducationDto, responseMyInfoDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseProfileDto);
    }
}
