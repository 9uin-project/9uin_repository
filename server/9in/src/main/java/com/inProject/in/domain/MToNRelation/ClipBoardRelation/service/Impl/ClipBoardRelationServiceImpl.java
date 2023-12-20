package com.inProject.in.domain.MToNRelation.ClipBoardRelation.service.Impl;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.config.security.JwtTokenProvider;
import com.inProject.in.domain.Board.Dto.response.ResponseBoardListDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto.RequestSearchClipDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto.ResponseClipBoardRelationDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.entity.ClipBoardRelation;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.repository.ClipBoardRelationRepository;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.service.ClipBoardRelationService;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.entity.RoleBoardRelation;
import com.inProject.in.domain.MToNRelation.TagBoardRelation.entity.TagBoardRelation;
import com.inProject.in.domain.RoleNeeded.Dto.ResponseRoleNeededDto;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
public class ClipBoardRelationServiceImpl implements ClipBoardRelationService {

    private UserRepository userRepository;
    private BoardRepository boardRepository;
    private ClipBoardRelationRepository clipBoardRelationRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final Logger log = LoggerFactory.getLogger(ClipBoardRelationServiceImpl.class);

    @Autowired
    public ClipBoardRelationServiceImpl(UserRepository userRepository,
                                        BoardRepository boardRepository,
                                        ClipBoardRelationRepository clipBoardRelationRepository,
                                        JwtTokenProvider jwtTokenProvider){

        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
        this.clipBoardRelationRepository = clipBoardRelationRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public List<ResponseBoardListDto> getClipedBoards(Pageable pageable, RequestSearchClipDto requestSearchClipDto,  HttpServletRequest request) {
        List<ResponseBoardListDto> responseBoardDtoList = new ArrayList<>();
        User user = getUserFromRequest(request);
        String title = requestSearchClipDto.getTitle();
        String type = requestSearchClipDto.getType();
        List<String> tags = requestSearchClipDto.getTags();

        Page<Board> boardPage = boardRepository.searchBoardsByCliped(pageable, user, title, type, tags);
        List<Board> boardList = boardPage.getContent();

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

        log.info("clipService getClipedBoards ==> username : " + user.getUsername());

        return responseBoardDtoList;
    }

    @Override
    @Transactional
    public ResponseClipBoardRelationDto insertClip(Long user_id, Long board_id){


        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.CLIP, HttpStatus.NOT_FOUND, "insert Clip에서 유효하지 않은 user id : " + user_id));

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.CLIP, HttpStatus.NOT_FOUND, "insert Clip에서 유효하지 않은 post id : " + board_id));

        log.info("Using insertClip in clip Service ==> board_id : " + board_id + " user_id : " + user_id);

        //유저가 누른 적 없는 게시물인지 확인.
        if(clipBoardRelationRepository.isExistClipedBoard(user, board) == false) {

            ClipBoardRelation clipBoardRelation = ClipBoardRelation.builder()
                    .clipUser(user)
                    .clipedBoard(board)
                    .build();

            ClipBoardRelation createRelation = clipBoardRelationRepository.save(clipBoardRelation);
            ResponseClipBoardRelationDto responseClipBoardRelationDto = new ResponseClipBoardRelationDto("success", true);

            log.info("Insert Clip Post Relation ==> relation_id " + createRelation.getClipUser().getUsername());

            return responseClipBoardRelationDto;
        }

        return new ResponseClipBoardRelationDto("failed", false);  //유저가 누른 적 있는 경우 실패
    }

    @Override
    @Transactional
    public ResponseClipBoardRelationDto deleteClip(Long user_id, Long board_id) {

        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.CLIP, HttpStatus.NOT_FOUND, "delete Clip에서 유효하지 않은 user id : " + user_id));

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.CLIP, HttpStatus.NOT_FOUND, "delete Clip에서 유효하지 않은 post id : " + board_id));

        ClipBoardRelation clipBoardRelation = clipBoardRelationRepository.getClipedBoard(user, board)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.CLIP, HttpStatus.NOT_FOUND, "좋아요 등록이 되지 않은 게시글 ==> user id : " + user_id + " post id : " + board_id));

        log.info("Using deleteClip in clip service ==> board_id " + board_id + " user_id " + user_id);
        log.info("delete Clip Post Relation ==> relation_id " + clipBoardRelation.getId());

        clipBoardRelationRepository.delete(clipBoardRelation);
        return new ResponseClipBoardRelationDto("success", true);
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
