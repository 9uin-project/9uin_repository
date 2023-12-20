package com.inProject.in.domain.Board.repository.Impl;

import com.inProject.in.config.QueryDslConfig;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.entity.ClipBoardRelation;
import com.inProject.in.domain.MToNRelation.ClipBoardRelation.repository.ClipBoardRelationRepository;
import com.inProject.in.domain.MToNRelation.TagBoardRelation.entity.TagBoardRelation;
import com.inProject.in.domain.SkillTag.entity.SkillTag;
import com.inProject.in.domain.SkillTag.repository.SkillTagRepository;
import com.inProject.in.domain.MToNRelation.TagBoardRelation.repository.TagBoardRelationRepository;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
class CustomBoardRepositoryImplTest {

    @Autowired
    JPAQueryFactory jpaQueryFactory;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SkillTagRepository skilltagRepository;

    @Autowired
    TagBoardRelationRepository tagBoardRelationRepository;
    @Autowired
    ClipBoardRelationRepository clipBoardRelationRepository;


    @BeforeEach
    void dataSetting(){

        String []tagsname = new String[]{"react", "java", "python"};
        List<User> userList = new ArrayList<>();
        List<Board> boardList = new ArrayList<>();
        List<SkillTag> skillTagList = new ArrayList<>();
        List<TagBoardRelation> savedTagBoardRelationList = new ArrayList<>();

        for(int i = 0 ; i < 3; i++){
            skillTagList.add(
                    SkillTag.builder()
                            .name(tagsname[i])
                            .build()
            );
        }

        for(int i = 0 ; i < 10 ; i++){
            userList.add(
                    User.builder()
                            .username("user" + i)
                            .mail("user" + i + "@naver.com")
                            .password(Integer.toString(i))
                            .build()
            );
        }

        for(int i = 0 ; i< 50 ; i++){

            Board board = Board.builder()  //게시글 생성
                    .title("title" + i)
                    .text("text" + i)
                    .type((i % 2 == 0) ? "study" : "project")
                    .author(userList.get(i % 10))
                    .period(LocalDateTime.now())
                    .proceed_method("1 month")
                    .build();

            List<TagBoardRelation> tagBoardRelationList = new ArrayList<>();  //태그와 board 관계 저장할 리스트

            TagBoardRelation tagBoardRelation = TagBoardRelation.builder()    //tagBoardRelation 하나 생성
                    .board(board)
                    .skillTag(skillTagList.get(i % 3))
                    .build();

            if(i % 3 == 0){     //번호가 3의 배수면 그 게시글은 태그가 두 개
                TagBoardRelation tagBoardRelation1 = TagBoardRelation.builder()
                        .board(board)
                        .skillTag(skillTagList.get((int)((Math.random() * tagsname.length))))
                        .build();

                tagBoardRelationList.add(tagBoardRelation1);
            }

            tagBoardRelationList.add(tagBoardRelation);   //생성한 relation을 임시 리스트에 저장.

            board.setTagBoardRelationList(tagBoardRelationList);    //board의 set을 이용해 생성한 tagBoardRelation과 연결.
            boardList.add(board);

            savedTagBoardRelationList.add(tagBoardRelation); //db에 저장하기 위한 리스트에도 add.

            clipBoardRelationRepository.save(ClipBoardRelation.builder()  //즐겨찾기.
                    .clipedBoard(boardList.get(i))
                    .clipUser(userList.get(i % 10))
                    .build());
        }

        userRepository.saveAll(userList);
        boardRepository.saveAll(boardList);
        skilltagRepository.saveAll(skillTagList);
        tagBoardRelationRepository.saveAll(savedTagBoardRelationList);  //필수적으로 추가
}

    @Test
    @DisplayName("모든 게시글 조건없이 출력")
    void getBoardPageOne() {

        //given
        String title = "";
        String type = "";
        String user_id = "";
        List<String> tags = new ArrayList<>();

        //when
        long startmill = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Board> postPage = boardRepository.findBoards(pageable, user_id, title, type, tags);
        List<Board> boardList = postPage.getContent();
        long endmill = System.currentTimeMillis();
        long querytime = endmill - startmill;

        //then
        assertEquals(postPage.getSize(), 5);
        assertEquals(boardList.size(), 5);
        assertEquals(postPage.getTotalPages(), 10);

        for(Board board : boardList){
            System.out.println("--------------------------");
            System.out.println("Board content :" + board.getTitle() + ' ' +  board.getCreateAt());
            System.out.println("User id : " + board.getAuthor().getUsername());
            System.out.println("--------------------------");
        }
        System.out.println("querytime = " + querytime);
    }

    @Test
    @DisplayName("마지막 페이지 출력")
    void getBoardLastPage() {

        //given
        String title = "";
        String type = "";
        String user_id = "";
        List<String> tags = new ArrayList<>();

        //when
        long startmill = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(4, 5);
        Page<Board> postPage = boardRepository.findBoards(pageable, user_id, title, type, tags);
        List<Board> boardList = postPage.getContent();
        long endmill = System.currentTimeMillis();
        long querytime = endmill - startmill;

        //then
        assertEquals(postPage.getSize(), 5);
        assertEquals(boardList.size(), 5);
        assertEquals(postPage.getTotalPages(), 10);

        for(Board board : boardList){
            System.out.println("--------------------------");
            System.out.println("Board content :" + board.getTitle() + ' ' +  board.getCreateAt());
            System.out.println("User id : " + board.getAuthor().getUsername());
            System.out.println("--------------------------");
        }
        System.out.println("querytime = " + querytime);
    }

    @Test
    @DisplayName("중간 페이지 출력")
    void getBoardMiddlePage() {

        //given
        String title = "";
        String type = "";
        String user_id = "";
        List<String> tags = new ArrayList<>();

        //when
        long startmill = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(2, 5);
        Page<Board> postPage = boardRepository.findBoards(pageable, user_id, title, type, tags);
        List<Board> boardList = postPage.getContent();
        long endmill = System.currentTimeMillis();
        long querytime = endmill - startmill;

        //then
        assertEquals(postPage.getSize(), 5);
        assertEquals(boardList.size(), 5);
        assertEquals(postPage.getTotalPages(), 10);

        for(Board board : boardList){
            System.out.println("--------------------------");
            System.out.println("Board content :" + board.getTitle() + ' ' +  board.getCreateAt());
            System.out.println("User id : " + board.getAuthor().getUsername());
            System.out.println("--------------------------");
        }
        System.out.println("total page : " + postPage.getTotalPages());
        System.out.println("querytime : " + querytime);
    }

    @Test
    @DisplayName("작성자 이름 검색")
    void findUserBoard(){

        //given
        String title = "";
        String type = "";
        String user_id = "user1";
        List<String> tags = new ArrayList<>();


        //when
        long startmill = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(0, 5);
        Page<Board> postPage =  boardRepository.findBoards(pageable, user_id, title, type, tags);
        long endmill = System.currentTimeMillis();
        long querytime = endmill - startmill;

        //then
        assertEquals(postPage.getSize(), 5);
        List<Board> retBoardList = postPage.getContent();
        assertEquals(retBoardList.size(), 5);

        for(Board board : retBoardList){
            System.out.println("-----------------");
            System.out.println("Board user id " + board.getAuthor().getUsername());
            System.out.println("Board title " + board.getTitle());
            System.out.println("-----------------");
        }

        System.out.println("query time : " + querytime);
    }

    @Test
    @DisplayName("태그가 react인 게시글 필터링")
    void TagFiltering(){

        //given
        String title = "";
        String type = "";
        String user_id = "";
        List<String> tags = List.of("react");
        List<Board> boardList = new ArrayList<>();

        //when
        long startmill = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(0, 16);
        Page<Board> postPage = boardRepository.findBoards(pageable, user_id, title, type, tags);
        long endmill = System.currentTimeMillis();
        long querytime = endmill - startmill;

        //then
        assertEquals(postPage.getSize(), 16);
        List<Board> retBoardList = postPage.getContent();
        assertEquals(retBoardList.size(), 16);

        for(Board board : retBoardList){
            System.out.println("-----------------");
            System.out.println("Board user id " + board.getAuthor().getUsername());
            System.out.println("Board title " + board.getTitle());

            System.out.print("Tag : ");

            board.getTagBoardRelationList()
                    .stream().forEach(tag -> System.out.print(tag.getSkillTag().getName() + " "));

            System.out.println();
            System.out.println("-----------------");
        }

        System.out.println("query time : " + querytime);

    }

    @Test
    @DisplayName("유저들 본인이 즐겨찾기한 게시글만 출력되는 지 확인")
    void ClipedPosts(){
        //given
        String title = "";
        String type = "";
        String user_id = "";
        List<String> tags = new ArrayList<>();


        //when
        Pageable pageable = PageRequest.of(0, 5);

        for(int i = 0 ; i < 10; i++){
            User user = User.builder()
                    .id((long)i + 1)
                    .username("user" + i)
                    .build();

            Page<Board> postPage = boardRepository.searchBoardsByCliped(pageable, user, title, type, tags);
            List<Board> boardList = postPage.getContent();

            //then

            assertEquals(boardList.size(), 5);

            for(Board board : boardList){
                System.out.println("-----------------");
                System.out.println("username " + user.getUsername());
                System.out.println("Board board id " + board.getId());
                System.out.println("-----------------");
            }
        }

    }



}

