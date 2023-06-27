package com.inProject.in.domain.MToNRelation.RolePostRelation.entity;

import com.inProject.in.Global.BaseEntity;
import com.inProject.in.domain.Post.entity.Post;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "rolePostRelation")
public class RolePostRelation extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleNeeded roleNeeded;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}