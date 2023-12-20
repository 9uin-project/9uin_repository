package com.inProject.in.domain.CommonLogic.service.Impl;

import com.inProject.in.config.security.JwtTokenProvider;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.CommonLogic.Application.Dto.RequestApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.ResponseApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.service.ApplicationService;
import com.inProject.in.domain.CommonLogic.Application.service.Impl.ApplicationServiceImpl;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.entity.ApplicantBoardRelation;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.repository.ApplicantBoardRelationRepository;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.entity.ApplicantRoleRelation;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.repository.ApplicantRoleRelationRepository;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.entity.RoleBoardRelation;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.repository.RoleBoardRelationRepository;
import com.inProject.in.domain.MToNRelation.TagBoardRelation.entity.TagBoardRelation;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import com.inProject.in.domain.RoleNeeded.repository.RoleNeededRepository;
import com.inProject.in.domain.SkillTag.entity.SkillTag;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@Import(ApplicationServiceImpl.class)
class ApplicationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private RoleNeededRepository roleNeededRepository;
    @Mock
    private ApplicantBoardRelationRepository applicantBoardRelationRepository;
    @Mock
    private ApplicantRoleRelationRepository applicantRoleRelationRepository;
    @Mock
    private RoleBoardRelationRepository roleBoardRelationRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    ApplicationServiceImpl applicationService;

    private static User user;     //게시글 작성자
    private static User user2;  //지원자
    private static Board board;   //작성된 게시글
    private static SkillTag skillTag;
    private static RoleNeeded roleNeeded;
    private static TagBoardRelation tagBoardRelation;
    private static RoleBoardRelation roleBoardRelation;
    private static ApplicantBoardRelation applicantBoardRelation;
    private static ApplicantRoleRelation applicantRoleRelation;

    @BeforeEach
    void dataSet(){
        Long testid = 1L;

        user = User.builder()
                .username("user1")
                .password("1234")
                .mail("aa@naver.com")
                .build();

        user2 = User.builder()
                .id(2L)
                .username("user2")
                .password("1234")
                .mail("bb@naver.com")
                .build();

        board = Board.builder()
                .title("title1")
                .text("text1")
                .type("project")
                .period(LocalDateTime.parse("2023-12-31T23:59:59.999"))
                .proceed_method("")
                .author(user)
                .build();

        skillTag = SkillTag.builder()
                .name("react")
                .build();

        roleNeeded = RoleNeeded.builder()
                .name("frontend")
                .build();

        tagBoardRelation = TagBoardRelation.builder()
                .skillTag(skillTag)
                .board(board)
                .build();

        roleBoardRelation = RoleBoardRelation.builder()
                .roleNeeded(roleNeeded)
                .board(board)
                .pre_cnt(0)
                .want_cnt(4)
                .build();

        applicantBoardRelation = ApplicantBoardRelation.builder()
                .board(board)
                .board_applicant(user2)
                .build();

        applicantRoleRelation = ApplicantRoleRelation.builder()
                .role_applicant(user2)
                .roleNeeded(roleNeeded)
                .build();

        user.setId(testid);
        board.setId(testid);
        skillTag.setId(testid);
        roleNeeded.setId(testid);
        tagBoardRelation.setId(testid);
        roleBoardRelation.setId(testid);

        board.setTagBoardRelationList(List.of(tagBoardRelation));
        board.setRoleBoardRelationList(List.of(roleBoardRelation));


    }
    @Test
    @DisplayName("게시글의 직군에 지원하기")
    void applyToBoard() {

        //given

        RequestApplicationDto requestApplicationDto = RequestApplicationDto.builder()
                .board_id(1L)
                .user_id(2L)
                .role_id(1L)
                .authorName("user1")
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-AUTH-TOKEN", "token");

        given(jwtTokenProvider.resolveToken(request)).willReturn("token");   //getUserFromRequest 메서드 시작.
        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn("user2");
        given(userRepository.getByUsername(anyString())).willReturn(Optional.ofNullable(user2)); //끝

        given(boardRepository.findById(1L)).willReturn(Optional.ofNullable(board));
        given(roleNeededRepository.findById(1L)).willReturn(Optional.ofNullable(roleNeeded));
        given(applicantBoardRelationRepository.isExistApplicantBoard(user2, board)).willReturn(false);
        given(roleBoardRelationRepository.findRelationById(1L, 1L)).willReturn(Optional.ofNullable(roleBoardRelation));
        given(applicantBoardRelationRepository.save(any(ApplicantBoardRelation.class))).willReturn(applicantBoardRelation);
        given(applicantRoleRelationRepository.save(any(ApplicantRoleRelation.class))).willReturn(applicantRoleRelation);

        //when
        ResponseApplicationDto responseApplicationDto =  applicationService.createApplication(requestApplicationDto, request);


        //then
        ApplicantBoardRelation applicantBoardRelation1 = board.getApplicantBoardRelationList().get(0);
        assertEquals(responseApplicationDto.getMessage(), "success");
        assertEquals(applicantBoardRelation1.getBoard_applicant(), user2);
        assertEquals(applicantBoardRelation1.getBoard(), board);
    }

}