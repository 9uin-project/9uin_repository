package com.inProject.in.domain.Profile.service;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.MToNRelation.TagMyInfoRelation.entity.TagMyInfoRelation;
import com.inProject.in.domain.Profile.Dto.response.*;
import com.inProject.in.domain.Profile.entity.*;
import com.inProject.in.domain.SkillTag.Dto.ResponseSkillTagDto;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;



@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    @Autowired
    public ProfileService(UserRepository userRepository,
                          BoardRepository boardRepository){
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional
    public ResponseProfileDto getProfile(String username){

        User user = userRepository.getByUsername(username)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.PROFILE, HttpStatus.NOT_FOUND, "ProfileController getProfile에서 잘못된 username : " + username));

        List<ResponseCertificateDto> responseCertificateDtoList = new ArrayList<>();
        List<ResponseProject_skillDto> responseProjectSkillDtoList = new ArrayList<>();
        List<ResponseEducationDto> responseEducationDtoList = new ArrayList<>();
        List<ResponseJob_exDto> responseJobExDtoList = new ArrayList<>();
        MyInfo myInfo = user.getMyInfo();

        for(Certificate certificate : user.getCertificateList()) responseCertificateDtoList.add(new ResponseCertificateDto(certificate));
        for(Project_skill project_skill : user.getProjectSkillList()) responseProjectSkillDtoList.add(new ResponseProject_skillDto(project_skill));
        for(Education education : user.getEducationList()) responseEducationDtoList.add(new ResponseEducationDto(education));
        for(Job_ex jobEx : user.getJobExList()) responseJobExDtoList.add(new ResponseJob_exDto(jobEx));

        ResponseMyInfoDto responseMyInfoDto = new ResponseMyInfoDto(myInfo);

        for(TagMyInfoRelation tagMyInfoRelation : myInfo.getTagMyInfoRelationList()){
            responseMyInfoDto.getResponseSkillTagDtoList().add(new ResponseSkillTagDto(tagMyInfoRelation.getSkillTag()));
        }
//
//        Long clipedCount = boardRepository.CountsClipedBoards(user);
//        Long projectCount = boardRepository.CountsUserBoards(user, "프로젝트");
//        Long studyCount = boardRepository.CountsUserBoards(user, "스터디");
        Long clipedCount = boardRepository.getClipedCount(user, "", "", List.of()).fetchOne();
        Long projectCount = boardRepository.getCount(username, "", "프로젝트", List.of()).fetchOne();
        Long studyCount = boardRepository.getCount(username, "", "스터디", List.of()).fetchOne();


        ResponseProfileDto responseProfileDto = ResponseProfileDto.builder()
                .certificateDtoList(responseCertificateDtoList)
                .jobExDtoList(responseJobExDtoList)
                .educationDtoList(responseEducationDtoList)
                .projectSkillDtoList(responseProjectSkillDtoList)
                .myInfoDto(responseMyInfoDto)
                .clipedCounts(clipedCount)
                .projectCounts(projectCount)
                .studyCounts(studyCount)
                .build();

        return responseProfileDto;
    }


}
