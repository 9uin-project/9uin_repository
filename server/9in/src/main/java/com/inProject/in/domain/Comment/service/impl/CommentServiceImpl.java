package com.inProject.in.domain.Comment.service.impl;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.config.security.JwtTokenProvider;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.Comment.Dto.RequestCommentDto;
import com.inProject.in.domain.Comment.Dto.ResponseCommentDto;
import com.inProject.in.domain.Comment.Dto.UpdateCommentDto;
import com.inProject.in.domain.Comment.entity.Comment;
import com.inProject.in.domain.Comment.repository.CommentRepository;
import com.inProject.in.domain.Comment.service.CommentService;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private UserRepository userRepository;
    private BoardRepository boardRepository;
    private CommentRepository commentRepository;
    private JwtTokenProvider jwtTokenProvider;
    private final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              UserRepository userRepository,
                              BoardRepository boardRepository,
                              JwtTokenProvider jwtTokenProvider) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public ResponseCommentDto getComment(Long id) {
        Comment comment = commentRepository.findById(id).
                orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.COMMENT, HttpStatus.NOT_FOUND, id + "는 getComment에서 유효하지 않은 id값 입니다."));

        ResponseCommentDto responseCommentDto = ResponseCommentDto.builder()
                .comment_id(comment.getId())
                .text(comment.getText())
                .build();

        return responseCommentDto;
    }

    @Override
    @Transactional
    public ResponseCommentDto createComment(RequestCommentDto requestCommentDto, HttpServletRequest request) {

        Long board_id = requestCommentDto.getBoard_id();
        User user = getUserFromRequest(request);

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.COMMENT, HttpStatus.NOT_FOUND,  board_id + "는 createBoard에서 유효하지 않은 게시글 id값 입니다."));

        board.setComment_cnt(board.getComment_cnt() + 1);

//        Comment comment = Comment.builder()
//                .user(user)
//                .board(board)
//                .text(requestCommentDto.getText())
//                .build();

        Comment comment = requestCommentDto.toEntity(user, board);

        Comment savedComment = commentRepository.save(comment);
        Long savedId = savedComment.getId();
        log.info("CreateComment in commentService ==> comment id : " + savedId + " username : " + user.getUsername());
        log.info("comment count increase : " + board.getComment_cnt());

        ResponseCommentDto responseCommentDto = new ResponseCommentDto(savedComment);

        return responseCommentDto;
    }

    @Override
    @Transactional
    public ResponseCommentDto updateComment(Long id, UpdateCommentDto updateCommentDto, HttpServletRequest request) {
        User user = getUserFromRequest(request);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.COMMENT, HttpStatus.NOT_FOUND, id + "는 updateComment에서 유효하지 않은 id값 입니다."));

        if(!user.getUsername().equals(comment.getUser().getUsername())){
            throw new CustomException(ConstantsClass.ExceptionClass.COMMENT, HttpStatus.UNAUTHORIZED, "권한이 없습니다.");
        }

        comment.updateComment(updateCommentDto);
        Comment updatedComment = commentRepository.save(comment);

        ResponseCommentDto responseCommentDto = new ResponseCommentDto(updatedComment);
        log.info("CommentService updateComment : " + responseCommentDto.getComment_id() + responseCommentDto.getText());

        return responseCommentDto;
    }

    @Override
    @Transactional
    public List<ResponseCommentDto> getCommentsInBoard(Pageable pageable, Long board_id) {

        Page<Comment> commentPage =  commentRepository.findCommentsByBoardId(pageable, board_id);
        List<Comment> commentList = commentPage.getContent();
        List<ResponseCommentDto> responseCommentDtoList = new ArrayList<>();

        for(Comment comment : commentList){
            ResponseCommentDto responseCommentDto = new ResponseCommentDto(comment);
            responseCommentDtoList.add(responseCommentDto);
        }

        return responseCommentDtoList;
    }

    @Override
    @Transactional
//    @PreAuthorize("#comment.user.username == authentication.principle.username or hasRole('ADMIN')")
    public void deleteComment(Long id, HttpServletRequest request) {
        User user = getUserFromRequest(request);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.COMMENT, HttpStatus.NOT_FOUND, id + "는 deleteComment에서 유효하지 않은 id값 입니다."));

        if(!user.getUsername().equals(comment.getUser().getUsername()) && !request.isUserInRole("ROLE_ADMIN") ){
            throw new CustomException(ConstantsClass.ExceptionClass.COMMENT, HttpStatus.UNAUTHORIZED, "작성자만 댓글을 삭제할 수 있습니다. 권한이 없습니다.");
        }
        int cnt = comment.getBoard().getComment_cnt();

        comment.getBoard().setComment_cnt(cnt - 1);
        commentRepository.deleteById(id);

        log.info("CommentService deleteComment ==> username : " + user.getUsername());
        log.info("authorization ADMIN : " + request.isUserInRole("ROLE_ADMIN"));
    }

    private User getUserFromRequest(HttpServletRequest request){
        String token = jwtTokenProvider.resolveToken(request);
        User user;

        if(token != null && jwtTokenProvider.validateToken(token)){
            String username = jwtTokenProvider.getUsername(token);

            return user = userRepository.getByUsername(username)
                    .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.USER, HttpStatus.NOT_FOUND, username + "은 유효하지 않은 username 입니다."));
        }
        else{
            throw new CustomException(ConstantsClass.ExceptionClass.USER, HttpStatus.UNAUTHORIZED, "token이 없거나, 권한이 유효하지 않습니다.");
        }


    }
}
