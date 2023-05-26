package com.inProject.in.domain.Post.service;

import com.inProject.in.domain.Post.Dto.PostDto;
import com.inProject.in.domain.Post.Dto.ResponsePostDto;
import com.inProject.in.domain.Post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


public interface PostService {
    ResponsePostDto getPost(Long id);

    ResponsePostDto createPost(PostDto postDto);

    ResponsePostDto updatePost(Long id, PostDto postDto);

    void deletePost(Long id);

    Page<ResponsePostDto> getUserPostList(Long id, Pageable pageable);

}
