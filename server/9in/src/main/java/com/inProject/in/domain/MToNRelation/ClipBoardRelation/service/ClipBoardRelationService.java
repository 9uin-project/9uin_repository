package com.inProject.in.domain.MToNRelation.ClipBoardRelation.service;

import com.inProject.in.domain.Board.Dto.response.ResponseBoardListDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto.RequestSearchClipDto;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto.ResponseClipBoardRelationDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClipBoardRelationService {
    List<ResponseBoardListDto> getClipedBoards(Pageable pageable, RequestSearchClipDto requestSearchClipDto, HttpServletRequest request);
    ResponseClipBoardRelationDto insertClip(Long user_id, Long post_id);
    ResponseClipBoardRelationDto deleteClip(Long user_id, Long post_id);
}
