package com.inProject.in.domain.Board.Dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSearchBoardDto {
    @Builder.Default
    private String username = "";
    @Builder.Default
    private String title = "";
    @Builder.Default
    private String type = "";
    @Builder.Default
    private List<String> tags = new ArrayList<>();  //url에 작성안하면 디폴트로 적용되는 값들 작성. 필터링할 때.
    @Builder.Default
    private List<String> roles = new ArrayList<>();
}