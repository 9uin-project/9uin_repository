package com.inProject.in.domain.CommonLogic.service.Impl;

import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.CommonLogic.Dto.ApplicationDto;
import com.inProject.in.domain.CommonLogic.Dto.ResponseApplicationDto;
import com.inProject.in.domain.CommonLogic.service.ApplicationService;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.entity.ApplicantBoardRelation;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.repository.ApplicantBoardRelationRepository;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.entity.ApplicantRoleRelation;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.repository.ApplicantRoleRelationRepository;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.entity.RoleBoardRelation;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.repository.RoleBoardRelationRepository;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import com.inProject.in.domain.RoleNeeded.repository.RoleNeededRepository;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private UserRepository userRepository;
    private BoardRepository boardRepository;
    private RoleNeededRepository roleNeededRepository;
    private ApplicantBoardRelationRepository applicantBoardRelationRepository;
    private ApplicantRoleRelationRepository applicantRoleRelationRepository;
    private RoleBoardRelationRepository roleBoardRelationRepository;
    private final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    public ApplicationServiceImpl(UserRepository userRepository,
                                  BoardRepository boardRepository,
                                  RoleNeededRepository roleNeededRepository,
                                  ApplicantBoardRelationRepository applicantBoardRelationRepository,
                                  ApplicantRoleRelationRepository applicantRoleRelationRepository,
                                  RoleBoardRelationRepository roleBoardRelationRepository){

        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.roleNeededRepository = roleNeededRepository;
        this.applicantBoardRelationRepository = applicantBoardRelationRepository;
        this.applicantRoleRelationRepository = applicantRoleRelationRepository;
        this.roleBoardRelationRepository = roleBoardRelationRepository;
    }


    @Override
    public ResponseApplicationDto createApplication(ApplicationDto applicationDto) {  //사용자가 지원 버튼 눌렀을 때 로직

        Long user_id = applicationDto.getUser_id();
        Long board_id = applicationDto.getBoard_id();
        Long role_id = applicationDto.getRole_id();

        User user = userRepository.findById(applicationDto.getUser_id())
                .orElseThrow(() -> new IllegalArgumentException("applyToBoard 에서 유효하지 않은 user id : " + user_id));

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new IllegalArgumentException("applyToBoard 에서 유효하지 않은 post id : " + board_id));

        RoleNeeded roleNeeded = roleNeededRepository.findById(role_id)
                .orElseThrow(() -> new IllegalArgumentException("applyToBoard 에서 유효하지 않은 role id : " + role_id));

        if(applicantBoardRelationRepository.isExistApplicantBoard(user, board) == false) {  //이미 지원한 게시글에는 지원 불가

            ApplicantBoardRelation applicantBoardRelation = ApplicantBoardRelation.builder()
                    .board_applicant(user)
                    .board(board)
                    .build();

            ApplicantRoleRelation applicantRoleRelation = ApplicantRoleRelation.builder()
                    .role_applicant(user)
                    .roleNeeded(roleNeeded)
                    .build();

            RoleBoardRelation roleBoardRelation = roleBoardRelationRepository.findRelationById(board_id, role_id).get();   //Optional 처리 생각

            int pre_cnt = roleBoardRelation.getPre_cnt();
            int want_cnt = roleBoardRelation.getWant_cnt();

            if(pre_cnt < want_cnt){
                roleBoardRelation.setPre_cnt(pre_cnt + 1);
                RoleBoardRelation updateRoleBoard = roleBoardRelationRepository.save(roleBoardRelation);
                log.info("Update in insert role - post relation ==> role - post relation_id : " + updateRoleBoard.getId() +
                        " relation pre_cnt : " + updateRoleBoard.getPre_cnt() + " relation want_cnt : " + updateRoleBoard.getWant_cnt());
            }
            else{
                //초과 시 로직.
            }

            ApplicantBoardRelation createApplicantBoardRelation = applicantBoardRelationRepository.save(applicantBoardRelation);
            ApplicantRoleRelation createApplicantRoleRelation = applicantRoleRelationRepository.save(applicantRoleRelation);

            log.info("Insert application ==> user - post relation_id : " + createApplicantBoardRelation.getId() +
                    " user - role relation_id : " + createApplicantRoleRelation.getId());

            ResponseApplicationDto responseApplicationDto = ResponseApplicationDto.builder()
                    .applicant_id(user_id)
                    .author_id(board.getAuthor().getId())
                    .board_id(board_id)
                    .role_id(role_id)
                    .build();

            return responseApplicationDto;
        }
        return null;
    }

    @Override
    public void deleteApplication(ApplicationDto applicationDto) {
        Long board_id = applicationDto.getBoard_id();
        Long role_id = applicationDto.getRole_id();
        Long user_id = applicationDto.getUser_id();
        int pre_cnt;
        int want_cnt;

        User user = userRepository.findById(applicationDto.getUser_id())
                .orElseThrow(() -> new IllegalArgumentException("applyToBoard 에서 유효하지 않은 user id : " + user_id));

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new IllegalArgumentException("applyToBoard 에서 유효하지 않은 post id : " + board_id));

        RoleNeeded roleNeeded = roleNeededRepository.findById(role_id)
                .orElseThrow(() -> new IllegalArgumentException("applyToBoard 에서 유효하지 않은 role id : " + role_id));

        if(applicantBoardRelationRepository.isExistApplicantBoard(user, board) == true){

            ApplicantBoardRelation applicantBoardRelation = applicantBoardRelationRepository.findApplicantBoard(user, board).get();
            ApplicantRoleRelation applicantRoleRelation = applicantRoleRelationRepository.findApplicantRole(user, roleNeeded).get();

            applicantBoardRelationRepository.deleteById(applicantBoardRelation.getId());
            applicantRoleRelationRepository.deleteById(applicantRoleRelation.getId());

            log.info("Delete user - board relation ==> user id : " + user_id + " board id : " + board_id + " relation id " + applicantBoardRelation.getId());
            log.info("Delete user - role relation ==> role id : " + role_id + " relation id " + applicantRoleRelation.getId());

            RoleBoardRelation roleBoardRelation = roleBoardRelationRepository.findRelationById(board_id, role_id).get();

            pre_cnt = roleBoardRelation.getPre_cnt();
            want_cnt = roleBoardRelation.getWant_cnt();

            if(pre_cnt > 0){
                roleBoardRelation.setPre_cnt(pre_cnt - 1);
                RoleBoardRelation updateRoleBoard = roleBoardRelationRepository.save(roleBoardRelation);
                log.info("Update in delete role - post relation ==> role - post relation_id : " + updateRoleBoard.getId() +
                        " relation pre_cnt : " + updateRoleBoard.getPre_cnt() + " relation want_cnt : " + updateRoleBoard.getWant_cnt());
            }
            else{
                throw new IllegalArgumentException("delete application 에서 이미 인원 수가 0인 것 감소");
            }
        }
        else{
            throw new IllegalArgumentException("cannot delete");
        }
    }
}