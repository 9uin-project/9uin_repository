package com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.repository.Impl;

import com.inProject.in.domain.Board.entity.Board;
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
    public Optional<ApplicantRoleRelation> findApplicantRole(User user, Board board) { //여기 board 추가할 것.
        ApplicantRoleRelation query = jpaQueryFactory.selectFrom(qApplicantRoleRelation)
                .where(qApplicantRoleRelation.role_applicant.eq(user),
                        qApplicantRoleRelation.board.eq(board))
                .fetchFirst();

        return Optional.of(query);
    }
}
