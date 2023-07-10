package com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.repository.Impl;

import com.inProject.in.domain.MToNRelation.ApplicantPostRelation.entity.ApplicantPostRelation;
import com.inProject.in.domain.MToNRelation.ApplicantPostRelation.entity.QApplicantPostRelation;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.entity.ApplicantRoleRelation;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.entity.QApplicantRoleRelation;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.repository.CustomApplicantRoleRepository;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import com.inProject.in.domain.User.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class CustomApplicantRoleRepositoryImpl implements CustomApplicantRoleRepository {

    private QApplicantRoleRelation qApplicantRoleRelation = QApplicantRoleRelation.applicantRoleRelation;
    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public CustomApplicantRoleRepositoryImpl(JPAQueryFactory jpaQueryFactory){
        this.jpaQueryFactory = jpaQueryFactory;
    }
    @Override
    public Boolean isExistApplicantRole(User user, RoleNeeded roleNeeded) {
        ApplicantRoleRelation query = jpaQueryFactory.selectFrom(qApplicantRoleRelation)
                .where(qApplicantRoleRelation.role_applicant.eq(user),
                        qApplicantRoleRelation.roleNeeded.eq(roleNeeded))
                .fetchFirst();

        return query != null;
    }

    @Override
    public Optional<ApplicantRoleRelation> findApplicantRole(User user, RoleNeeded roleNeeded) {
        ApplicantRoleRelation query = jpaQueryFactory.selectFrom(qApplicantRoleRelation)
                .where(qApplicantRoleRelation.role_applicant.eq(user),
                        qApplicantRoleRelation.roleNeeded.eq(roleNeeded))
                .fetchOne();

        return Optional.of(query);
    }
}
