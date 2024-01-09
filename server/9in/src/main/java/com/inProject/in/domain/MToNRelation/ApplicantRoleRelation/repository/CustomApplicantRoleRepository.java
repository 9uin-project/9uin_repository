package com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.repository;

import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.entity.ApplicantRoleRelation;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import com.inProject.in.domain.User.entity.User;

import java.util.Optional;

public interface CustomApplicantRoleRepository {
    Boolean isExistApplicantRole(User user, RoleNeeded roleNeeded);
    Optional<ApplicantRoleRelation> findApplicantRole(User user, Board board);
}
