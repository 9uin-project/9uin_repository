package com.inProject.in.domain.Board.repository;

import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.User.entity.User;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface CustomBoardRepository {
    JPAQuery<Long> getCount(String username, String title, String type, List<String> tags);
    JPAQuery<Long> getClipedCount(User user, String title, String type, List<String> tags);
    Page<Board> findBoards(Pageable pageable, String user_id, String title, String type, List<String> tags);
    Page<Board> searchBoardsByCliped(Pageable pageable, User user, String title, String type, List<String> tags);
    Page<Board> searchBoardsByUserInfo(Pageable pageable, User user, String type);
//    Long CountsClipedBoards(User user);
//    Long CountsUserBoards(User user, String type);

}
