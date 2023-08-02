package com.inProject.in.domain.Comment.Dto;

import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Comment.entity.Comment;
import com.inProject.in.domain.User.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentDto {
    private Long user_id;
    private Long board_id;   //추후에 순환참조 문제 발생 시 이것으로 교체.
    private String text;

    public Comment toEntity(User user, Board board){

        return Comment.builder()
                .user(user)
                .board(board)
                .text(text)
                .build();
    }

    public CommentDto(Comment comment){
        this.user_id = comment.getUser().getId();
        this.board_id = comment.getBoard().getId();
        this.text = comment.getText();
    }
}
