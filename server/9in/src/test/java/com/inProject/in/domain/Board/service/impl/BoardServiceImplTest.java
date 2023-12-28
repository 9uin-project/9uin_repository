package com.inProject.in.domain.Board.service.impl;

import com.inProject.in.config.security.JwtTokenProvider;
import com.inProject.in.domain.Board.Dto.request.RequestBoardDto;
import com.inProject.in.domain.Board.Dto.request.RequestCreateBoardDto;
import com.inProject.in.domain.Board.Dto.request.RequestSearchBoardDto;
import com.inProject.in.domain.Board.Dto.request.RequestUpdateBoardDto;
import com.inProject.in.domain.Board.Dto.response.ResponseBoardListDto;
import com.inProject.in.domain.Board.Dto.response.ResponsePagingBoardDto;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Board.repository.ViewCountRepository;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.entity.ClipBoardRelation;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.entity.RoleBoardRelation;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.repository.RoleBoardRelationRepository;
import com.inProject.in.domain.MToNRelation.TagBoardRelation.entity.TagBoardRelation;
import com.inProject.in.domain.MToNRelation.TagBoardRelation.repository.TagBoardRelationRepository;
import com.inProject.in.domain.Board.Dto.response.ResponseBoardDto;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.Board.service.BoardService;
import com.inProject.in.domain.RoleNeeded.Dto.RequestUsingInBoardDto;
import com.inProject.in.domain.RoleNeeded.Dto.ResponseRoleNeededDto;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import com.inProject.in.domain.RoleNeeded.repository.RoleNeededRepository;
import com.inProject.in.domain.SkillTag.Dto.RequestSkillTagDto;
import com.inProject.in.domain.SkillTag.entity.SkillTag;
import com.inProject.in.domain.SkillTag.repository.SkillTagRepository;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)   //mockito 의 mock객체를 사용하기 위한 어노테이션.
@Import(BoardServiceImpl.class)
class BoardServiceImplTest {
    @Mock
    BoardRepository boardRepository;
    @Mock
    SkillTagRepository skillTagRepository;
    @Mock
    RoleNeededRepository roleNeededRepository;
    @Mock
    TagBoardRelationRepository tagBoardRelationRepository;
    @Mock
    RoleBoardRelationRepository roleBoardRelationRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    ViewCountRepository viewCountRepository;

    @InjectMocks                                       //생성한 mock 객체를 주입받음.
    BoardServiceImpl boardService;

    private static User user;     //게시글 작성자
    private static Board board;   //작성된 게시글
    private static SkillTag skillTag;
    private static RoleNeeded roleNeeded;
    private static SkillTag skillTag2;
    private static RoleNeeded roleNeeded2;
    private static TagBoardRelation tagBoardRelation;
    private static TagBoardRelation tagBoardRelation2;
    private static RoleBoardRelation roleBoardRelation;
    private static RoleBoardRelation roleBoardRelation2;
    @BeforeEach
    void dataSet(){
        Long testid = 1L;

        user = User.builder()
                .username("user1")
                .password("1234")
                .mail("aa@naver.com")
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

        skillTag2 = SkillTag.builder()
                .name("spring")
                .build();

        roleNeeded2 = RoleNeeded.builder()
                .name("backend")
                .build();

        tagBoardRelation = TagBoardRelation.builder()
                .skillTag(skillTag)
                .board(board)
                .build();

        tagBoardRelation2 = TagBoardRelation.builder()
                .skillTag(skillTag2)
                .board(board)
                .build();

        roleBoardRelation = RoleBoardRelation.builder()
                .roleNeeded(roleNeeded)
                .board(board)
                .pre_cnt(0)
                .want_cnt(4)
                .build();

        roleBoardRelation2 = RoleBoardRelation.builder()
                .roleNeeded(roleNeeded2)
                .board(board)
                .pre_cnt(0)
                .want_cnt(4)
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
    @DisplayName("로그인 안 한 유저가 게시글 하나 조회하는 로직")
    void getBoardWithoutLogin() {

        //given

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-AUTH-TOKEN", "token");

        given(jwtTokenProvider.resolveToken(request)).willReturn(null);
        given(boardRepository.findById(1L)).willReturn(Optional.ofNullable(board));


        //when
        ResponseBoardDto responseBoardDto = boardService.getBoard(1L, request);

        //then
        assertEquals(board.getId(), responseBoardDto.getBoard_id());
        assertEquals(board.getView_cnt(), responseBoardDto.getView_cnt());
        assertEquals(responseBoardDto.getView_cnt(), 0);
        assertEquals(responseBoardDto.getTags(), List.of("react"));
        assertEquals(responseBoardDto.getRoles(), List.of(new ResponseRoleNeededDto(1L, "frontend", 0, 4, false)));

        System.out.println("--------------");
        System.out.println("view cnt : " + responseBoardDto.getView_cnt());
        System.out.println("--------------");
    }

    @Test
    @DisplayName("로그인 한 유저가 게시글 하나 조회")
    void getBoardWithLogin(){
        //given
        //게시글 하나 만들고 연관관계 설정

        User user2 = User.builder()  //게시글 조회하는 사람
                .username("user2")
                .password("1234")
                .mail("bb@naver.com")
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-AUTH-TOKEN", "token");

        given(jwtTokenProvider.resolveToken(request)).willReturn("token");   //getUserFromRequest 메서드 시작.
        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn("user2");
        given(userRepository.getByUsername(anyString())).willReturn(Optional.ofNullable(user2)); //끝

        given(viewCountRepository.getBoardList(anyString())).willReturn(new ArrayList<>());   //게시글 조회자는 직전까지 이 게시글을 조회한 적 없다.
        given(boardRepository.updateViewCnt(any(Long.class))).willReturn(board.getView_cnt() + 1);

        board.setView_cnt(board.getView_cnt() + 1);           //updateViewCnt가 무조건 잘 실행되었다고 생각하고 진행.

        given(boardRepository.findById(1L)).willReturn(Optional.ofNullable(board));

        //when
        ResponseBoardDto responseBoardDto = boardService.getBoard(1L, request);

        //then
        assertEquals(board.getId(), responseBoardDto.getBoard_id());
        assertEquals(board.getView_cnt(), responseBoardDto.getView_cnt());
        assertEquals(responseBoardDto.getView_cnt(), 1);

        System.out.println("--------------");
        System.out.println("view cnt : " + responseBoardDto.getView_cnt());
        System.out.println("--------------");

    }

    @Test
    @DisplayName("게시글 하나 생성하는 로직")
    void createBoard() {

        //given
        Long testId = 1l;

        RequestSkillTagDto requestSkillTagDto = new RequestSkillTagDto("react");

        RequestUsingInBoardDto requestUsingInBoardDto = RequestUsingInBoardDto.builder()  //태그, 직군 dto
                .name("frontend")
                .pre_cnt(0)
                .want_cnt(4)
                .build();

        RequestCreateBoardDto requestCreateBoardDto = RequestCreateBoardDto.builder() //createBoard 요청시 필요한 board dto
                .title("title1")
                .text("text1")
                .type("project")
                .period(LocalDateTime.now())
                .proceed_method("")
                .tagDtoList(List.of(requestSkillTagDto))
                .roleNeededDtoList(List.of(requestUsingInBoardDto))
                .build();

        RoleBoardRelation savedRoleBoardRelation = new RoleBoardRelation();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-AUTH-TOKEN", "token");

        given(jwtTokenProvider.resolveToken(request)).willReturn("token");  //getUserFromRequest 시작
        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn("user1");
        given(userRepository.getByUsername(anyString())).willReturn(Optional.ofNullable(user));  //끝

        given(boardRepository.save(any(Board.class))).willReturn(board);
        given(skillTagRepository.findTagByName(skillTag.getName())).willReturn(Optional.of(skillTag));
        given(roleNeededRepository.findRoleByName(any(String.class))).willReturn(Optional.of(roleNeeded));
        given(tagBoardRelationRepository.save(any(TagBoardRelation.class))).willReturn(tagBoardRelation);
        given(roleBoardRelationRepository.save(any(RoleBoardRelation.class))).willReturn(roleBoardRelation);


        //when
        ResponseBoardDto responseBoardDto = boardService.createBoard(requestCreateBoardDto, request);

        //then
        assertEquals(responseBoardDto.getBoard_id(), board.getId());
        assertEquals(responseBoardDto.getTitle(), board.getTitle());
        assertEquals(responseBoardDto.getText(), board.getText());
        assertEquals(responseBoardDto.getUsername(), board.getAuthor().getUsername());
        assertEquals(responseBoardDto.getTags(), List.of("react"));
        assertEquals(responseBoardDto.getRoles(), List.of(new ResponseRoleNeededDto(1L, "frontend", 0, 4, false)));
    }

    @Test
    @DisplayName("게시글 수정 로직")
    void updateBoard() {
        //given

        RequestSkillTagDto requestSkillTagDto = new RequestSkillTagDto("spring");

        RequestUsingInBoardDto requestUsingInBoardDto = RequestUsingInBoardDto.builder()  //태그, 직군 dto
                .name("backend")
                .pre_cnt(0)
                .want_cnt(4)
                .build();

        RequestUpdateBoardDto requestUpdateBoardDto = RequestUpdateBoardDto.builder()
                .title("title2")
                .text("text2")
                .type("project")
                .period(LocalDateTime.now())
                .proceed_method("")
                .requestUsingInBoardDtoList(List.of(requestUsingInBoardDto))
                .requestSkillTagDtoList(List.of(requestSkillTagDto))
                .build();

        Board board2 = Board.builder()   //수정된 후 결과 게시글은 이렇게 되어야 한다. id값은 변경되지 않는다.
                .id(1L)
                .title("title2")
                .text("text2")
                .type("project")
                .period(LocalDateTime.now())
                .proceed_method("")
                .author(user)
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-AUTH-TOKEN", "token");

        ResponseBoardDto ExpectResponseBoardDto = new ResponseBoardDto(board2);

        given(jwtTokenProvider.resolveToken(request)).willReturn("token");  //getUserFromRequest 시작
        given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
        given(jwtTokenProvider.getUsername(anyString())).willReturn("user1");
        given(userRepository.getByUsername(anyString())).willReturn(Optional.ofNullable(user));  //끝

        given(boardRepository.findById(1L)).willReturn(Optional.ofNullable(board));
        given(roleNeededRepository.findRoleByName(anyString())).willReturn(Optional.of(roleNeeded2));
        given(roleBoardRelationRepository.save(any(RoleBoardRelation.class))).willReturn(roleBoardRelation2);
        given(skillTagRepository.findTagByName(anyString())).willReturn(Optional.of(skillTag2));
        given(tagBoardRelationRepository.save(any(TagBoardRelation.class))).willReturn(tagBoardRelation2);
        given(boardRepository.save(any(Board.class))).willAnswer(invocation -> {   //willAnswer를 통해 입력값에 따라 다르게 반환값을 가지도록 함.
            Board savedBoard = invocation.getArgument(0);                    //save 실행 시 매개변수 첫 번째 값 가져옴.
            return savedBoard;
        });


        //when
        ResponseBoardDto responseBoardDto = boardService.updateBoard(1L, requestUpdateBoardDto, request);


        //then
        assertEquals(responseBoardDto.getBoard_id(), board2.getId());
        assertEquals(responseBoardDto.getBoard_id(), board.getId());

        assertEquals(responseBoardDto.getTitle(), board2.getTitle());
        assertEquals(responseBoardDto.getText(), board2.getText());
        assertEquals(responseBoardDto.getProceed_method(), board2.getProceed_method());

        System.out.println("--------------");
        System.out.println("after : " + responseBoardDto.getTitle());
        System.out.println("--------------");


    }



    @Test
    @DisplayName("게시글 리스트 출력 : 8개 이하 페이지")
    void getBoardList() {

        //given


        Pageable pageable = PageRequest.of(0 , 3);
        String username = "";
        String title = "";
        String type = "";
        List<String> tags = new ArrayList<>();
        List<String> roles = new ArrayList<>();

        RequestSearchBoardDto requestSearchBoardDto = RequestSearchBoardDto.builder()
                .build();

        Board board2 = Board.builder()
                .title("title2")
                .text("text2")
                .type("project")
                .period(LocalDateTime.now())
                .proceed_method("")
                .author(user)
                .tagBoardRelationList(List.of(tagBoardRelation))
                .roleBoardRelationList(List.of(roleBoardRelation))
                .build();

        Board board3 = Board.builder()
                .title("title3")
                .text("text2")
                .type("project")
                .period(LocalDateTime.now())
                .proceed_method("")
                .author(user)
                .tagBoardRelationList(List.of(tagBoardRelation))
                .roleBoardRelationList(List.of(roleBoardRelation))
                .build();

        List<Board> boardList = List.of(board, board2, board3);
        Page<Board> page = new PageImpl<>(boardList, pageable, boardList.size()); //이렇게 해도 가능!

        given(boardRepository.findBoards(pageable, username, title, type, tags, roles)).willReturn(page);

        //when
        ResponsePagingBoardDto responseBoardDtoList = boardService.getBoardList(pageable, requestSearchBoardDto);

        //then
        int i = 1;
        assertEquals(3, responseBoardDtoList.getContent().size());

        for(ResponseBoardListDto responseBoardDto : responseBoardDtoList.getContent()){
            assertEquals(responseBoardDto.getTitle(), "title" + i);
            System.out.println("responseBoardDto.getTitle() = " + responseBoardDto.getTitle());
            i++;
        }
    }
}