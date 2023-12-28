package com.inProject.in.domain.CommonLogic.Application.service.Impl;

import com.inProject.in.Global.exception.ConstantsClass;
import com.inProject.in.Global.exception.CustomException;
import com.inProject.in.config.security.JwtTokenProvider;
import com.inProject.in.domain.Board.entity.Board;
import com.inProject.in.domain.CommonLogic.Application.Dto.RequestApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.ResponseApplicationDto;
import com.inProject.in.domain.CommonLogic.Application.Dto.ResponseSseDto;
import com.inProject.in.domain.CommonLogic.Application.service.ApplicationService;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.entity.ApplicantBoardRelation;
import com.inProject.in.domain.MToNRelation.ApplicantBoardRelation.repository.ApplicantBoardRelationRepository;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.entity.ApplicantRoleRelation;
import com.inProject.in.domain.MToNRelation.ApplicantRoleRelation.repository.ApplicantRoleRelationRepository;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.entity.RoleBoardRelation;
import com.inProject.in.domain.MToNRelation.RoleBoardRelation.repository.RoleBoardRelationRepository;
import com.inProject.in.domain.Board.repository.BoardRepository;
import com.inProject.in.domain.Notification.entity.Notification;
import com.inProject.in.domain.Notification.repository.NotificationRepository;
import com.inProject.in.domain.RoleNeeded.entity.RoleNeeded;
import com.inProject.in.domain.RoleNeeded.repository.RoleNeededRepository;
import com.inProject.in.domain.User.entity.User;
import com.inProject.in.domain.User.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final RoleNeededRepository roleNeededRepository;
    private final ApplicantBoardRelationRepository applicantBoardRelationRepository;
    private final ApplicantRoleRelationRepository applicantRoleRelationRepository;
    private final RoleBoardRelationRepository roleBoardRelationRepository;
    private final NotificationRepository notificationRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    public ApplicationServiceImpl(UserRepository userRepository,
                                  BoardRepository boardRepository,
                                  RoleNeededRepository roleNeededRepository,
                                  ApplicantBoardRelationRepository applicantBoardRelationRepository,
                                  ApplicantRoleRelationRepository applicantRoleRelationRepository,
                                  RoleBoardRelationRepository roleBoardRelationRepository,
                                  NotificationRepository notificationRepository,
                                  JwtTokenProvider jwtTokenProvider){

        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.roleNeededRepository = roleNeededRepository;
        this.applicantBoardRelationRepository = applicantBoardRelationRepository;
        this.applicantRoleRelationRepository = applicantRoleRelationRepository;
        this.roleBoardRelationRepository = roleBoardRelationRepository;
        this.notificationRepository = notificationRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    @Transactional
    public ResponseApplicationDto createApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request) throws CustomException{  //사용자가 지원 버튼 눌렀을 때 로직

        User user = getUserFromRequest(request);
        Long board_id = requestApplicationDto.getBoard_id();
        Long role_id = requestApplicationDto.getRole_id();

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, board_id + "는 applyToBoard 에서 유효하지 않은 board id"));

        RoleNeeded roleNeeded = roleNeededRepository.findById(role_id)
                .orElseThrow(() -> new  CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, role_id + "는 applyToBoard 에서 유효하지 않은 role id"));

        if(board.getAuthor().getId() == user.getId()){
            throw new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.CONFLICT, "작성자는 지원 불가");
        }

        boolean isExist = applicantBoardRelationRepository.isExistApplicantBoard(user, board);

        log.info("지원여부 : " + isExist);


        //지원 로직 start. -> application Check repo로 넘겨서 저장후 거기서 또 관리 . 또는 컬럼 하나 만들어서 상태를 보관.

        if(isExist == false) {  //이미 지원한 게시글에는 지원 불가

            ApplicantBoardRelation applicantBoardRelation = ApplicantBoardRelation.builder()
                    .board_applicant(user)
                    .board(board)
                    .status(1)
                    .build();

            ApplicantRoleRelation applicantRoleRelation = ApplicantRoleRelation.builder()
                    .role_applicant(user)
                    .roleNeeded(roleNeeded)
                    .board(board)
                    .build();

            RoleBoardRelation roleBoardRelation = roleBoardRelationRepository.findRelationById(board_id, role_id)
                    .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, role_id + "는 이 게시글에 등록되지 않음"));

            int want_cnt = roleBoardRelation.getWant_cnt();
            int pre_cnt = roleBoardRelation.getPre_cnt();

            if(want_cnt <= pre_cnt)
                throw new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.CONFLICT, "최대 지원 수를 초과했습니다.");

            ApplicantBoardRelation createApplicantBoardRelation = applicantBoardRelationRepository.save(applicantBoardRelation);
            ApplicantRoleRelation createApplicantRoleRelation = applicantRoleRelationRepository.save(applicantRoleRelation);

            board.getApplicantBoardRelationList().add(createApplicantBoardRelation);

            log.info("Insert application ==> user - board relation_id : " + createApplicantBoardRelation.getId() +
                    " user - role relation_id : " + createApplicantRoleRelation.getId());

            ResponseApplicationDto responseApplicationDto = new ResponseApplicationDto("create", "success", true);
            return responseApplicationDto;
        }
        else{
            throw new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.BAD_REQUEST, "이미 지원한 게시글");
        }

    }

    @Override
    @Transactional
    public ResponseApplicationDto deleteApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request) {

        User user = getUserFromRequest(request);

        Long board_id = requestApplicationDto.getBoard_id();
        Long role_id = requestApplicationDto.getRole_id();

        int pre_cnt;

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, board_id + "는 applyToBoard 에서 유효하지 않은 board id"));

        RoleNeeded roleNeeded = roleNeededRepository.findById(role_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, role_id + "는 applyToBoard 에서 유효하지 않은 role id"));

        if (applicantBoardRelationRepository.isExistApplicantBoard(user, board) == true) {

            ApplicantBoardRelation applicantBoardRelation = applicantBoardRelationRepository.findApplicantBoard(user, board).get();
            ApplicantRoleRelation applicantRoleRelation = applicantRoleRelationRepository.findApplicantRole(user, board).get();

            applicantBoardRelationRepository.deleteById(applicantBoardRelation.getId());
            applicantRoleRelationRepository.deleteById(applicantRoleRelation.getId());

            log.info("Delete user - board relation ==> user id : " + user.getId() + " board id : " + board_id + " relation id " + applicantBoardRelation.getId());
            log.info("Delete user - role relation ==> role id : " + role_id + " relation id " + applicantRoleRelation.getId());

            RoleBoardRelation roleBoardRelation = roleBoardRelationRepository.findRelationById(board_id, role_id).get();

            pre_cnt = roleBoardRelation.getPre_cnt();

            if (pre_cnt > 0) {
//                roleBoardRelation.setPre_cnt(pre_cnt - 1);
//                RoleBoardRelation updateRoleBoard = roleBoardRelationRepository.save(roleBoardRelation);
//                log.info("Update in delete role - post relation ==> role - post relation_id : " + updateRoleBoard.getId() +
//                        " relation pre_cnt : " + updateRoleBoard.getPre_cnt() + " relation want_cnt : " + updateRoleBoard.getWant_cnt());

                ResponseApplicationDto responseApplicationDto = new ResponseApplicationDto("delete", "success", true);

                return responseApplicationDto;
            } else {
                throw new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.CONFLICT, "delete application 에서 이미 인원 수가 0인 것 감소");
            }
        }
        else {
            throw new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.BAD_REQUEST, "지원 기록이 없음.");
        }
    }


    @Override
    @Transactional
    public ApplicantBoardRelation rejectApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request){
        User user = getUserFromRequest(request);
        Long board_id = requestApplicationDto.getBoard_id();
        Long role_id = requestApplicationDto.getRole_id();
//        Long user_id = requestApplicationDto.getUser_id();

//        User user = userRepository.findById(requestApplicationDto.getUser_id())
//                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, user_id + "는 applyToBoard 에서 유효하지 않은 user id"));

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, board_id + "는 applyToBoard 에서 유효하지 않은 board id"));

        ApplicantBoardRelation applicantBoardRelation = applicantBoardRelationRepository.findApplicantBoard(user, board).get();
        //applicantBoardRelation.setStatus(3);
        //delete()
        return applicantBoardRelation;
    }
    @Override
    @Transactional
    public ApplicantBoardRelation acceptApplication(RequestApplicationDto requestApplicationDto, HttpServletRequest request){
        User user = getUserFromRequest(request);

        Long board_id = requestApplicationDto.getBoard_id();
        Long role_id = requestApplicationDto.getRole_id();
//        Long user_id = requestApplicationDto.getUser_id();

//        User user = userRepository.findById(requestApplicationDto.getUser_id())
//                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, user_id + "는 applyToBoard 에서 유효하지 않은 user id"));

        Board board = boardRepository.findById(board_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, board_id + "는 applyToBoard 에서 유효하지 않은 board id"));

        RoleBoardRelation roleBoardRelation = roleBoardRelationRepository.findRelationById(board_id, role_id)
                .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.NOT_FOUND, board_id + "에 " + role_id + "간의 관계는 없습니다."));

        int pre_cnt = roleBoardRelation.getPre_cnt();
        int want_cnt = roleBoardRelation.getWant_cnt();

        if(want_cnt > pre_cnt){
            roleBoardRelation.setPre_cnt(roleBoardRelation.getPre_cnt() + 1);
            RoleBoardRelation updateRoleBoardRelation = roleBoardRelationRepository.save(roleBoardRelation);
        }
        else{
            throw new CustomException(ConstantsClass.ExceptionClass.APPLICATION, HttpStatus.CONFLICT, "최대 지원 수를 초과했습니다.");
        }

        ApplicantBoardRelation applicantBoardRelation = applicantBoardRelationRepository.findApplicantBoard(user, board).get();
        applicantBoardRelation.setStatus(0);
        return applicantBoardRelation;
    }
    @Override
    @Transactional
    public ResponseSseDto ApplicationToSseResponse(RequestApplicationDto requestApplicationDto){
        String board_title = boardRepository.getById(requestApplicationDto.getBoard_id()).getTitle();
        String role_name = roleNeededRepository.findById(requestApplicationDto.getRole_id()).get().getName();
//        String user_name = userRepository.getById(requestApplicationDto.getUser_id()).getUsername();
        ResponseSseDto responseSseDto =  ResponseSseDto.builder().
                title(board_title).
                role(role_name).
                build();
        return responseSseDto;
    }

    private User getUserFromRequest(HttpServletRequest request){
        String token = jwtTokenProvider.resolveToken(request);
        User user;

        if(token != null && jwtTokenProvider.validateToken(token)){
            String username = jwtTokenProvider.getUsername(token);

            return user = userRepository.getByUsername(username)
                    .orElseThrow(() -> new CustomException(ConstantsClass.ExceptionClass.BOARD, HttpStatus.NOT_FOUND, "applicationService ==> request로부터 user를 찾지 못함"));
        }
        else{
            throw new CustomException(ConstantsClass.ExceptionClass.USER, HttpStatus.UNAUTHORIZED, "token이 없거나, 권한이 유효하지 않습니다.");
        }
    }

}