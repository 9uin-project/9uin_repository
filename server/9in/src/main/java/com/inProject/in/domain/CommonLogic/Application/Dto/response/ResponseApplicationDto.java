package com.inProject.in.domain.CommonLogic.Application.Dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ResponseApplicationDto {
    private String type;   //삭제인지 지원인지 구분
    private String message;
    private boolean success;

}
