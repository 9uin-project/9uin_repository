package com.inProject.in.domain.User.repository;

import com.inProject.in.domain.User.repository.UserRepository;
import com.inProject.in.domain.User.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;


    @Test
    public void CRUD(){

        //given
        User user = User.builder()
                .user_id("user1")
                .password("1234")
                .mail("abcd@naver.com")
                .build();                //빌더 패턴을 사용해서 객체 생성.

        //when (create)
        User savedUser = userRepository.save(user);

        //then
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getUser_id(), savedUser.getUser_id());
        assertEquals(user.getPassword(), savedUser.getPassword());
        assertEquals(user.getMail(), savedUser.getMail());

        //when (read)
        User getUser = userRepository.findById(user.getId()).get();

        //then
        assertEquals(user.getId(), getUser.getId());
        assertEquals(user.getUser_id(), getUser.getUser_id());
        assertEquals(user.getPassword(), getUser.getPassword());
        assertEquals(user.getMail(), getUser.getMail());


        //when(update)
        User updateUser = userRepository.findById(user.getId()).get();
        updateUser.setUser_id("user100");
        User updatedUser = userRepository.save(updateUser);

        //then
        assertEquals(updatedUser.getUser_id(), "user100");


        //when(delete)
        userRepository.deleteById(user.getId());

        //then
        assertFalse(userRepository.findById(user.getId()).isPresent());
    }
}