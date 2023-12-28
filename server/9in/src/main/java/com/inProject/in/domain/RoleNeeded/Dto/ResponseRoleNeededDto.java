package com.inProject.in.domain.RoleNeeded.Dto;

import com.inProject.in.domain.MToNRelation.RoleBoardRelation.entity.RoleBoardRelation;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ResponseRoleNeededDto {
    private Long role_id;
    private String name;
    private int pre_cnt;
    private int want_cnt;
    @Builder.Default
    private boolean isApply = false;

    public ResponseRoleNeededDto(RoleBoardRelation roleBoardRelation){
        this.role_id = roleBoardRelation.getRoleNeeded().getId();
        this.name = roleBoardRelation.getRoleNeeded().getName();
        this.pre_cnt = roleBoardRelation.getPre_cnt();
        this.want_cnt = roleBoardRelation.getWant_cnt();
    }
}
