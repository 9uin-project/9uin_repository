package com.inProject.in.domain.CommonLogic.Application.service;

import com.inProject.in.domain.CommonLogic.Application.Dto.request.RequestAcceptDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.request.RequestApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.response.ResponseApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.response.ResponseSseDto;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.entity.ApplicantBoardRelation;
import jakarta.servlet.http.HttpServletRequest;

public interface ApplicationService {
    ResponseApplicationDto createApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request);
    ResponseApplicationDto deleteApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request);
    ApplicantBoardRelation rejectApplication(RequestAcceptDto requestAcceptDto, HttpServletRequest request);
    ApplicantBoardRelation acceptApplication(RequestAcceptDto requestAcceptDto, HttpServletRequest request);
    ResponseSseDto ApplicationToSseResponse(Long board_id, Long role_id);
}

