package com.inProject.in.domain.MToNRelation.ClipBoardRelation.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestSearchClipDto {
    @Builder.Default
    private String title = "";
    @Builder.Default
    private String type = "";
    @Builder.Default
    private List<String> tags = new ArrayList<>();  //url에 작성안하면 디폴트로 적용되는 값들 작성. 필터링할 때.
}
