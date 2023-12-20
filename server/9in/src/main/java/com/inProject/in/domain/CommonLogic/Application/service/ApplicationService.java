package com.inProject.in.domain.CommonLogic.Application.service;

import com.inProject.in.domain.CommonLogic.Application.Dto.RequestApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.ResponseApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.ResponseSseDto;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.entity.ApplicantBoardRelation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface ApplicationService {
    ResponseApplicationDto createApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request);
    ResponseApplicationDto deleteApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request);
    ApplicantBoardRelation rejectApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request);
    ApplicantBoardRelation acceptApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request);
    ResponseSseDto ApplicationToSseResponse(RequestApplicationDto requestApplicationDto);
}

