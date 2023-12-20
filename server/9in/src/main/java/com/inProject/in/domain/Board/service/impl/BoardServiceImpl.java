package com.inProject.in.domain.Board.service.impl;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.config.security.JwtTokenProvider;
import com.inProject.in.domain.Board.Dto.request.RequestCreateBoardDto;
import com.inProject.in.domain.Board.Dto.request.RequestSearchBoardDto;
import com.inProject.in.domain.Board.Dto.request.RequestUpdateBoardDto;
import com.inProject.in.domain.Board.Dto.response.ResponseBoardListDto;
import com.inProject.in.domain.Board.Dto.response.ResponsePagingBoardDto;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Board.repository.ViewCountRepository;
import com.inProject.in.domain.Comment.Dto.ResponseCommentDto;
import com.inProject.in.domain.Comment.entity.Comment;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.entity.ApplicantBoardRelation;
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
import com.inProject.in.domain.User.Dto.ResponseInfoInBoardDto;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final SkillTagRepository skilltagRepository;
    private final RoleNeededRepository roleNeededRepository;
    private final TagBoardRelationRepository tagBoardRelationRepository;
    private final RoleBoardRelationRepository roleBoardRelationRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ViewCountRepository viewCountRepository;
    private final Logger log = LoggerFactory.getLogger(BoardServiceImpl.class);

    public List<TagBoardRelation> InsertTagBoardRelation(Board board, List<RequestSkillTagDto> requestSkillTagDtoList){

        List<TagBoardRelation> tagBoardRelationList = new ArrayList<>();

        if(board.getTagBoardRelationList() != null){             //이미 태그가 들어있는 게시글 -> 업데이트를 한다는 것.
            for(TagBoardRelation tagBoardRelation : board.getTagBoardRelationList()){
                tagBoardRelationRepository.delete(tagBoardRelation);
            }
        }

        for(RequestSkillTagDto requestSkillTagDto : requestSkillTagDtoList){   //받아온 태그 당 관계 데이터를 생성

            SkillTag skillTag = skilltagRepository.findTagByName(requestSkillTagDto.toEntity().getName())
                    .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, requestSkillTagDto.getName() + "는 없는 태그명입니다."));    //태그 잘못 입력했으면 오류 반환

            TagBoardRelation tagBoardRelation = TagBoardRelation.builder()
                    .skillTag(skillTag)
                    .board(board)
                    .build();

            tagBoardRelationList.add(tagBoardRelationRepository.save(tagBoardRelation));
        }

        return tagBoardRelationList;
    }

    public List<RoleBoardRelation> InsertRolePostRelation(Board board, List<RequestUsingInBoardDto> requestRoleNeededDtoList){

        List<RoleBoardRelation> roleBoardRelationList = new ArrayList<>();

        if(board.getRoleBoardRelationList() != null){
            for(RoleBoardRelation roleBoardRelation : board.getRoleBoardRelationList()){
                roleBoardRelationRepository.delete(roleBoardRelation);
            }
        }

        for(RequestUsingInBoardDto requestRoleNeededDto : requestRoleNeededDtoList){

            RoleNeeded roleNeeded = roleNeededRepository.findRoleByName(requestRoleNeededDto.getName())
                    .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, requestRoleNeededDto.getName() + "는 없는 직군명입니다."));  //직군이 DB에 이미 정해진 대로 저장되어있다는 점.

            RoleBoardRelation roleBoardRelation = RoleBoardRelation.builder()
                    .pre_cnt(requestRoleNeededDto.getPre_cnt())
                    .want_cnt(requestRoleNeededDto.getWant_cnt())                    //roleNeeded엔티티에는 pre, want카운트가 없음. 하지만 dto에는 있음. 입력하기 편하도록.
                    .roleNeeded(roleNeeded)
                    .board(board)
                    .build();

            RoleBoardRelation savedRoleBoardRelation = roleBoardRelationRepository.save(roleBoardRelation);
            log.info("request want cnt : " + requestRoleNeededDto.getWant_cnt());
            log.info("saved want cnt : " + savedRoleBoardRelation.getWant_cnt());
            roleBoardRelationList.add(savedRoleBoardRelation);
        }
        return roleBoardRelationList;
    }


    @Override
    @Transactional
    public ResponseBoardDto getBoard(Long id, HttpServletRequest request) {

        String token = jwtTokenProvider.resolveToken(request);

        if(token != null){                                //사용자가 로그인한 회원인 경우만 조회수를 올리도록 했다.
            User user = getUserFromRequest(request);
            String str_board = String.valueOf(id);
            String str_user = String.valueOf(user.getId());

            if(!viewCountRepository.getBoardList(str_user).contains(str_board)){
                viewCountRepository.setBoard(str_user, str_board);
//            board.setView_cnt(board.getView_cnt() + 1);
                int view = boardRepository.updateViewCnt(id);          //두 메서드 중 무엇을 쓸까. 한 번 테스트하기.
                log.info("view cnt up : " + view );
            }
        }

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, id + "는 유효하지 않은 게시글 id입니다." ));

        log.info("view cnt : " + board.getView_cnt());
        ResponseBoardDto responseBoardDto = new ResponseBoardDto(board);

        for(TagBoardRelation tagBoardRelation : board.getTagBoardRelationList()){
            responseBoardDto.getTags().add(tagBoardRelation.getSkillTag().getName());
        }

        for(RoleBoardRelation roleBoardRelation : board.getRoleBoardRelationList()){
            ResponseRoleNeededDto responseRoleNeededDto = new ResponseRoleNeededDto(roleBoardRelation);
            responseBoardDto.getRoles().add(responseRoleNeededDto);
        }

        if(board.getCommentList() != null){
            for(Comment comment : board.getCommentList()){
                ResponseCommentDto responseCommentDto = new ResponseCommentDto(comment);
                responseBoardDto.getCommentList().add(responseCommentDto);
            }
        }

        if(board.getApplicantBoardRelationList() != null){
            for(ApplicantBoardRelation applicantBoardRelation : board.getApplicantBoardRelationList()){
                if(applicantBoardRelation.getStatus() == 0) {    //게시글 참여 승낙이 된 사람만 출력.
                    ResponseInfoInBoardDto responseInfoInBoardDto = new ResponseInfoInBoardDto(applicantBoardRelation);

                    responseBoardDto.getResponseInfoInBoardDtoList().add(responseInfoInBoardDto);
                }
            }
        }
        return responseBoardDto;
    }

    @Override
    @Transactional
    public ResponseBoardDto createBoard(//Long user_id, 없애도 될 것 같다.
                                        RequestCreateBoardDto requestCreateBoardDto,
                                        HttpServletRequest request) throws CustomException {

        try {
            User user = getUserFromRequest(request);
            Board board = requestCreateBoardDto.toBoardDto().toEntity();
            List<RequestSkillTagDto> requestSkillTagDtoList = requestCreateBoardDto.getTagDtoList();
            List<RequestUsingInBoardDto> requestRoleNeededDtoList = requestCreateBoardDto.getRoleNeededDtoList();

            board.setAuthor(user);

            Board createBoard = boardRepository.save(board);

            List<TagBoardRelation> tagBoardRelationList = InsertTagBoardRelation(board, requestSkillTagDtoList);
            List<RoleBoardRelation> roleBoardRelationList = InsertRolePostRelation(board, requestRoleNeededDtoList);  //DB에 연관 데이터인 태그, 직군 데이터 삽입
            board.setRoleBoardRelationList(roleBoardRelationList);
            board.setTagBoardRelationList(tagBoardRelationList);

            log.info("BoardService createPost : " + createBoard.getId() + " " + createBoard.getTitle());

            for(TagBoardRelation tagBoardRelation : tagBoardRelationList){
                log.info("Insert Tag - Board relation ==> board id : " + tagBoardRelation.getBoard().getId() + " tag id : "
                        + tagBoardRelation.getSkillTag().getId());
            }

            for(RoleBoardRelation roleBoardRelation : roleBoardRelationList){
                log.info("Insert Role - Board relation ==> Board id :" + roleBoardRelation.getBoard().getId()
                        + " role id : " + roleBoardRelation.getRoleNeeded().getId());
                log.info("pre cnt : " + roleBoardRelation.getPre_cnt());
                log.info("want cnt : " + roleBoardRelation.getWant_cnt());
            }

            ResponseBoardDto responseBoardDto = new ResponseBoardDto(createBoard );

            for(TagBoardRelation tagBoardRelation : board.getTagBoardRelationList()){
                responseBoardDto.getTags().add(tagBoardRelation.getSkillTag().getName());
            }

            for(RoleBoardRelation roleBoardRelation : board.getRoleBoardRelationList()){
                ResponseRoleNeededDto responseRoleNeededDto = new ResponseRoleNeededDto(roleBoardRelation);
                responseBoardDto.getRoles().add(responseRoleNeededDto);
            }

            if(board.getCommentList() != null){
                for(Comment comment : board.getCommentList()){
                    ResponseCommentDto responseCommentDto = new ResponseCommentDto(comment);
                    responseBoardDto.getCommentList().add(responseCommentDto);
                }
            }

            if(board.getApplicantBoardRelationList() != null){
                for(ApplicantBoardRelation applicantBoardRelation : board.getApplicantBoardRelationList()){
                    ResponseInfoInBoardDto responseInfoInBoardDto = new ResponseInfoInBoardDto(applicantBoardRelation);

                    responseBoardDto.getResponseInfoInBoardDtoList().add(responseInfoInBoardDto);
                }
            }

            return responseBoardDto;
        }catch (CustomException e){
            throw e;
        }



    }
    @Override
    @Transactional
//    @PreAuthorize("#board.author.username == authentication.principal.username")                                              //메서드 매개변수로 전달된 board를 참조한다. 여기서 #board는 SpEL(Spring Expression Language)을 통해
    public ResponseBoardDto updateBoard(Long id, RequestUpdateBoardDto requestUpdateBoardDto, HttpServletRequest request) {   //해당 메서드의 매개변수로 전달되지 않아도 메서드 내의 board를 참조한다.

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, id + "는 updateBoard에서 유효하지 않은 게시글 id"));

        User user = getUserFromRequest(request);

        if(board.getAuthor().getUsername() != user.getUsername()){
            throw new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.UNAUTHORIZED, "작성자만 게시글을 수정할 수 있습니다.");
        }

        log.info("BoardService updateBoard ==> new role : " + requestUpdateBoardDto.getRequestUsingInBoardDtoList().toString());
        log.info("BoardService updateBoard ==> new skill : " + requestUpdateBoardDto.getRequestSkillTagDtoList());

        List<RequestUsingInBoardDto> requestUsingInBoardDtoList = requestUpdateBoardDto.getRequestUsingInBoardDtoList();
        List<RequestSkillTagDto> requestSkillTagDtoList = requestUpdateBoardDto.getRequestSkillTagDtoList();

        List<RoleBoardRelation> roleBoardRelationList = InsertRolePostRelation(board, requestUsingInBoardDtoList);
        List<TagBoardRelation> tagBoardRelationList = InsertTagBoardRelation(board, requestSkillTagDtoList);

        board.updateBoard(requestUpdateBoardDto);

        board.setTagBoardRelationList(tagBoardRelationList);
        board.setRoleBoardRelationList(roleBoardRelationList);

        Board updatedBoard = boardRepository.save(board);
        ResponseBoardDto responseBoardDto = new ResponseBoardDto(updatedBoard);

        for(TagBoardRelation tagBoardRelation : board.getTagBoardRelationList()){
            responseBoardDto.getTags().add(tagBoardRelation.getSkillTag().getName());
        }

        for(RoleBoardRelation roleBoardRelation : board.getRoleBoardRelationList()){
            ResponseRoleNeededDto responseRoleNeededDto = new ResponseRoleNeededDto(roleBoardRelation);
            responseBoardDto.getRoles().add(responseRoleNeededDto);
        }

        if(board.getCommentList() != null){
            for(Comment comment : board.getCommentList()){
                ResponseCommentDto responseCommentDto = new ResponseCommentDto(comment);
                responseBoardDto.getCommentList().add(responseCommentDto);
            }
        }

        if(board.getApplicantBoardRelationList() != null){
            for(ApplicantBoardRelation applicantBoardRelation : board.getApplicantBoardRelationList()){
                ResponseInfoInBoardDto responseInfoInBoardDto = new ResponseInfoInBoardDto(applicantBoardRelation);

                responseBoardDto.getResponseInfoInBoardDtoList().add(responseInfoInBoardDto);
            }
        }

        log.info("BoardService updateBoard : " + responseBoardDto.getBoard_id() + responseBoardDto.getTitle());

        return responseBoardDto;
    }

    @Override
    @Transactional
   // @PreAuthorize("hasAuthority('ADMIN') or #board.author.username == authentication.principal.username")      //hasAuthority를 사용해야 함.여기서 hasRole은 단지 문자열 비교로
    public void deleteBoard(Long id, HttpServletRequest request) {                                                //권한을 확인하기에, 권한 계층을 고려하지 않음.

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, id + "는 deleteBoard에서 유효하지 않은 게시글 id" ));
        User user = getUserFromRequest(request);

        if(!user.getUsername().equals(board.getAuthor().getUsername()) && !request.isUserInRole("ROLE_ADMIN")){
            throw new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.UNAUTHORIZED, "권한이 없습니다.");
        }

        boardRepository.deleteById(id);

        log.info("BoardService deleteBoard ==> username : " + user.getUsername());
        log.info("authorization ADMIN : " + request.isUserInRole("ROLE_ADMIN"));
    }


    @Override
    @Transactional
    public ResponsePagingBoardDto getBoardList(Pageable pageable, RequestSearchBoardDto requestSearchBoardDto)  {  //Pageable pageable, String user_id, String title, String type, List<String> tags

        String username = requestSearchBoardDto.getUsername();
        String title = requestSearchBoardDto.getTitle();
        String type = requestSearchBoardDto.getType();
        List<String> tags = requestSearchBoardDto.getTags();

        Page<Board> boardPage = boardRepository.findBoards(pageable, username, title, type, tags);
        List<Board> boardList = boardPage.getContent();                                               //getContent를 통해 List로 변환시킨다.
        List<ResponseBoardListDto> responseBoardDtoList = new ArrayList<>();
        log.info("BoardService getBoardList ==> filtering by username : " + username + " title : " + title + " type : " + type );
        log.info("Tag filtering : " + tags.toString());

        for (Board board : boardList) {              //board들 돌면서 반환할 리스트 생성
            ResponseBoardListDto responseBoardListDto = new ResponseBoardListDto(board);

            for(TagBoardRelation tagBoardRelation : board.getTagBoardRelationList()){
                responseBoardListDto.getTags().add(tagBoardRelation.getSkillTag().getName());
            }
            for(RoleBoardRelation roleBoardRelation : board.getRoleBoardRelationList()){
                ResponseRoleNeededDto responseRoleNeededDto = new ResponseRoleNeededDto(roleBoardRelation);
                responseBoardListDto.getRoles().add(responseRoleNeededDto);
            }

            responseBoardDtoList.add(responseBoardListDto);
        }

        Long totalCount = boardPage.getTotalElements();    //전체 데이터 개수
        int totalPage = boardPage.getTotalPages();     //전체 페이지 개수


        ResponsePagingBoardDto responsePagingBoardDto = ResponsePagingBoardDto.builder()
                .content(responseBoardDtoList)
                .totalCount(totalCount)
                .totalPage(totalPage)
                .build();

        return responsePagingBoardDto;
    }

    @Override
    public List<ResponseBoardListDto> getBoardListForUserInfo(Pageable pageable, String username, String type) {
        User user = userRepository.getByUsername(username)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, "getBoardListByUser에서 username으로 user를 찾지 못함"));

        Page<Board> boardPage = boardRepository.searchBoardsByUserInfo(pageable, user, type);
        List<Board> boardList = boardPage.getContent();
        List<ResponseBoardListDto> responseBoardDtoList = new ArrayList<>();

        for (Board board : boardList) {
            ResponseBoardListDto responseBoardListDto = new ResponseBoardListDto(board);

            for(TagBoardRelation tagBoardRelation : board.getTagBoardRelationList()){
                responseBoardListDto.getTags().add(tagBoardRelation.getSkillTag().getName());
            }
            for(RoleBoardRelation roleBoardRelation : board.getRoleBoardRelationList()){
                ResponseRoleNeededDto responseRoleNeededDto = new ResponseRoleNeededDto(roleBoardRelation);
                responseBoardListDto.getRoles().add(responseRoleNeededDto);
            }

            responseBoardDtoList.add(responseBoardListDto);
        }

        return responseBoardDtoList;
    }


    private User getUserFromRequest(HttpServletRequest request){
        String token = jwtTokenProvider.resolveToken(request);
        User user;

        if(token != null && jwtTokenProvider.validateToken(token)){
            String username = jwtTokenProvider.getUsername(token);

            return user = userRepository.getByUsername(username)
                    .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, "BoardService ==> request로부터 user를 찾지 못함"));
        }
        else{
            throw new CustomException(ConstantsClass.ExceptionClass.USER, HttpStatus.UNAUTHORIZED, "token이 없거나, 권한이 유효하지 않습니다.");
        }
    }


}
