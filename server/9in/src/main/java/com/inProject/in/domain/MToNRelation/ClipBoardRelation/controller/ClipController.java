package com.inProject.in.domain.MToNRelation.ClipBoardRelation.controller;

import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.domain.Board.Dto.response.ResponseBoardListDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto.RequestClipDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto.RequestSearchClipDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto.ResponseClipBoardRelationDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.service.ClipBoardRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cliped")
@Tag(name = "clip", description = "즐겨찾기 관련 api")
public class ClipController {

    private ClipBoardRelationService clipBoardRelationService;

    @Autowired
    public ClipController(ClipBoardRelationService clipBoardRelationService){
        this.clipBoardRelationService = clipBoardRelationService;
    }
    @GetMapping()
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    @Operation(summary = "즐겨찾기 리스트 조회", description = "클립 설정한 게시글들을 가져옵니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "즐겨찾기 게시글 조회 성공", content = {
                            @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseBoardListDto.class))
                    })
            })
    public ResponseEntity<List<ResponseBoardListDto>> getClipedBoards(@PageableDefault(size = 8) Pageable pageable,
                                                                      RequestSearchClipDto requestSearchClipDto,
                                                                      HttpServletRequest request){
        try{
            List<ResponseBoardListDto> boardListDtoList = clipBoardRelationService.getClipedBoards(pageable, requestSearchClipDto, request);

            return ResponseEntity.status(HttpStatus.OK).body(boardListDtoList);
        }catch (CustomException e){
            throw e;
        }
    }

    @PostMapping()
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    @Operation(summary = "즐겨찾기 등록", description = "게시글을 클립 설정합니다.", responses = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기 취소", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResponseClipBoardRelationDto.class))
            })
    })
    public ResponseEntity<ResponseClipBoardRelationDto> CreateClipedBoard(@RequestBody RequestClipDto requestClipDto){
        Long user_id = requestClipDto.getUser_id();
        Long board_id = requestClipDto.getBoard_id();
        ResponseClipBoardRelationDto responseClipBoardRelationDto = clipBoardRelationService.insertClip(user_id , board_id);

        return ResponseEntity.status(HttpStatus.OK).body(responseClipBoardRelationDto);
    }

    @DeleteMapping()
    @Parameter(name = "X-AUTH-TOKEN", description = "토큰을 전송합니다.", in = ParameterIn.HEADER)
    @Operation(summary = "즐겨찾기 해제", description = "게시글을 클립 설정을 취소합니다.", responses = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기 취소", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = ResponseClipBoardRelationDto.class))
            })
    })
    public ResponseEntity<ResponseClipBoardRelationDto> deleteClipedBoard(@RequestBody RequestClipDto requestClipDto){
        Long user_id = requestClipDto.getUser_id();
        Long board_id = requestClipDto.getBoard_id();

        ResponseClipBoardRelationDto responseClipBoardRelationDto = clipBoardRelationService.deleteClip(user_id, board_id);

        return ResponseEntity.status(HttpStatus.OK).body(responseClipBoardRelationDto);
    }
}
