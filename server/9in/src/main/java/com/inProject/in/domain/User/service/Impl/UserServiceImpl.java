package com.inProject.in.domain.User.service.Impl;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.domain.User.Dto.ResponseUserDto;
import com.inProject.in.domain.User.Dto.UpdateUserDto;
import com.inProject.in.domain.User.Dto.RequestUserDto;
import com.inProject.in.domain.User.Mapper.UserMapper;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import com.inProject.in.domain.User.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public ResponseUserDto getUser(String username) {
        User user = userRepository.getByUsername(username)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.USER, HttpStatus.NOT_FOUND, "getUser에서 유효하지 않은 아이디" + username));

        ResponseUserDto responseUserDto = userMapper.toDto(user);

        return responseUserDto;
    }

    @Override
    public ResponseUserDto createUser(RequestUserDto userdto) {

        User user = userMapper.toEntity(userdto);
        User savedUser = userRepository.save(user);

        ResponseUserDto responseUserDto = userMapper.toDto(savedUser);

        return responseUserDto;
    }

    @Override
    @Transactional
    public ResponseUserDto updateUser(Long id, @RequestBody UpdateUserDto updateUserdto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("updateUser에서 유효하지 않은 id " + id));

        //빈 data가 들어왔을 때 예외처리 하는 코드 작성필요.
        //==
        user.updateUser(updateUserdto);
        User updatedUser = userRepository.save(user);

        ResponseUserDto responseUserDto = userMapper.toDto(updatedUser);
        return responseUserDto;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws CustomException {
        log.info("UserService loadUserByUsername ==> username : " + username);
        return userRepository.getByUsername(username)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.USER, HttpStatus.BAD_REQUEST, "loadUserByUsername에서 username으로 user 찾지 못함"));
    }
}
