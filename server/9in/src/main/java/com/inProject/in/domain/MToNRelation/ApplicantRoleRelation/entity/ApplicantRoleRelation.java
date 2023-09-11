package com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.entity;

import com.inProject.in.Global.BaseEntity;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import com.inProject.in.domain.User.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "applicantRoleRelation")
public class ApplicantRoleRelation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User role_applicant;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private RoleNeeded roleNeeded;
}
