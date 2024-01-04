package com.inProject.in.domain.CommonLogic.Application.Dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestApplicationDto {
    private Long board_id;
    private Long role_id;
    private String authorName;
}
