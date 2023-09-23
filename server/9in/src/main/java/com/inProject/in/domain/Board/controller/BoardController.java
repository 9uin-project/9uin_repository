package com.inProject.in.domain.Board.controller;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.domain.Board.Dto.*;
import com.inProject.in.domain.Board.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/boards")
@Tag(name = "board", description = "게시글 관련 api")
public class BoardController {
    private BoardService boardService;

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    @Autowired
    public BoardController(BoardService boardService){
        this.boardService = boardService;
    }

    @GetMapping("/{board_id}")
    @Parameter(name = "board_id", description = "게시글 ID", in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
    @Operation(summary = "게시글 조회", description = "게시글 하나를 조회합니다.",
    responses = {
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResponseBoardDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "게시글 조회 실패")
    })

    public ResponseEntity<ResponseBoardDto> getBoard(@PathVariable(name = "board_id") Long board_id) throws CustomException {
        try{
            ResponseBoardDto responseBoardDto = boardService.getBoard(board_id);

            return ResponseEntity.status(HttpStatus.OK).body(responseBoardDto);
        }catch (CustomException e){
            throw e;
        }
    }

    @GetMapping()
    @Operation(summary = "게시글 리스트 조회", description = "게시글 리스트를 페이지 단위로 가져옵니다.",
    responses = {
            @ApiResponse(responseCode = "200", description = "게시글 리스트 조회 성공", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResponseBoardListDto.class))
            })
    })
    public ResponseEntity<List<ResponseBoardListDto>> getBoards(@PageableDefault(size = 8) Pageable pageable,
                                                     RequestSearchBoardDto requestSearchBoardDto){
        try{
            List<ResponseBoardListDto> responseBoardDtoList = boardService.getBoardList(pageable, requestSearchBoardDto);

            return ResponseEntity.status(HttpStatus.OK).body(responseBoardDtoList);
        }catch (CustomException e){
            throw new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.BAD_REQUEST, "getBoards 페이징 오류");
        }
    }

    @GetMapping("/cliped")
    @Operation(summary = "즐겨찾기 리스트 조회", description = "클립 설정한 게시글들을 가져옵니다.",
            responses = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기 게시글 조회 성공", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResponseBoardListDto.class))
            })
    })
    public ResponseEntity<List<ResponseBoardListDto>> getClipedBoards(@PageableDefault(size = 8) Pageable pageable,
                                                                HttpServletRequest request){
        try{
            List<ResponseBoardListDto> boardListDtoList = boardService.getClipedBoards(pageable, request);

            return ResponseEntity.status(HttpStatus.OK).body(boardListDtoList);
        }catch (CustomException e){
            throw e;
        }
    }

    @PostMapping()
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)   //swagger에서 헤더로 토큰을 전송하기 위해 입력창을 만든다.
    @Operation(summary = "게시글 생성", description = "게시글 하나를 작성합니다.", responses = {
            @ApiResponse
    })
    public ResponseEntity<ResponseBoardDto> createBoard(@RequestBody RequestCreateBoardDto requestCreateBoardDto, HttpServletRequest request) throws CustomException{

//        RequestBoardDto requestBoardDto = requestCreateBoardDto.toBoardDto();
////        Long user_id = requestCreateBoardDto.getUser_id();
////        List<RequestSkillTagDto> requestSkillTagDtoList = new ArrayList<>();        //requestCreateDto에 requestSkilltagDto를 포함할까 고민중..
////        List<RequestUsingInBoardDto> requestRoleNeededDtoList = requestCreateBoardDto.getRoleNeededDtoList();
////
////        for(String tagName : requestCreateBoardDto.getTagNames()) {
////            requestSkillTagDtoList.add(new RequestSkillTagDto(tagName));
////        }
        try{
            ResponseBoardDto responseBoardDto = boardService.createBoard(requestCreateBoardDto, request);

            return ResponseEntity.status(HttpStatus.OK).body(responseBoardDto);
        }catch(CustomException e){
            throw e;
        }
    }

    @PutMapping("/{board_id}")
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    @Parameter(name = "board_id", description = "게시글 ID", in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
    @Operation(summary = "게시글 수정", description = "게시글 하나를 업데이트 합니다.")
    public ResponseEntity<ResponseBoardDto> updateBoard(@PathVariable(name = "board_id") Long board_id,
                                                        @RequestBody RequestUpdateBoardDto requestUpdateBoardDto,
                                                        HttpServletRequest request
    ){
        try{
            ResponseBoardDto responseBoardDto = boardService.updateBoard(board_id, requestUpdateBoardDto, request);
            return ResponseEntity.status(HttpStatus.OK).body(responseBoardDto);
        }catch(CustomException e){
            throw e;
        }
    }

    @DeleteMapping("/{board_id}")
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    @Parameter(name = "board_id", description = "게시글 ID", in = ParameterIn.PATH, schema = @Schema(type = "integer", format = "int64"))
    @Operation(summary = "게시글 삭제", description = "게시글 하나를 삭제합니다.")
    public ResponseEntity<String> deleteBoard(@PathVariable(name = "board_id") Long board_id, HttpServletRequest request){
        try{
            log.info("BoardController deleteBoard ==> header : " + request.getRequestURI());
            boardService.deleteBoard(board_id, request);
            return ResponseEntity.status(HttpStatus.OK).body("성공적으로 게시글 삭제");
        }catch(CustomException e){
            throw e;
        }
    }

}
