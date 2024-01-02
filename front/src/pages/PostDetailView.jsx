import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import { useSelector } from 'react-redux';
import { createAxiosInstance } from '../api/instance';
import useSSE from '../api/sse';
import { refreshTokenAndRetry } from '../api/user';

export default function PostDetail() {
  const token = useSelector((state) => state.auth.token);
  const username = useSelector((state) => state.auth.username);
  const user_id = useSelector((state) => state.auth.userId);

  const axiosInstance = createAxiosInstance(token);
  // const eventData = useSSE('/sse/connect');
  // console.log(eventData);

  const { board_id } = useParams();
  const [boardInfo, setBoardInfo] = useState(null);

  const [authorName, setAuthorName] = useState('');
  const [applyStatus, setApplyStatus] = useState({});

  // 댓글
  const [comment, setComment] = useState('');

  const fetchBoard = async () => {
    try {
      const response = await axiosInstance.get(`/boards/${board_id}`);
      setBoardInfo(response.data);
      setAuthorName(response.data.username);
    } catch (error) {
      console.error('Error fetching boarInfo', error);
      if (error.response.data.status === '401') {
        try {
          const retryResponse = await refreshTokenAndRetry(
            'get',
            `/boards/${board_id}`,
            {
              'X-AUTH-TOKEN': token,
            }
          );
          console.log('게시글 조회 성공 (재시도)');
          console.log(retryResponse);
        } catch (refreshError) {
          console.error('새로운 액세스 토큰 얻기 실패', refreshError);
        }
      }
    }
  };

  useEffect(() => {
    fetchBoard();
  }, [board_id]);

  const {
    title,
    type,
    proceed_method,
    period,
    roles,
    tags,
    createAt,
    view_cnt,
    commentList,
    text,
  } = boardInfo || {};
  console.warn([0]);

  const formattedPeriod = new Date(period).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });

  const formattedCreateAt = new Date(createAt).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });

  const finalDate = new Date(period);
  const createDate = new Date(createAt);
  const monthDifference =
    (createDate.getFullYear() - finalDate.getFullYear()) * 12 +
    (createDate.getMonth() - finalDate.getMonth());

  const handleAddClip = async () => {
    const clipInfo = {
      user_id,
      board_id,
    };

    try {
      const response = await axiosInstance.post('/cliped', clipInfo);
      console.log(response);
      alert('게시글을 즐겨찾기에 등록했습니다.');
    } catch (error) {
      console.log('즐겨찾기 등록 실패', error);
      alert('게시글 즐겨찾기 등록에 실패했습니다.');
    }
  };

  console.log(applyStatus);
  console.log(applyStatus[2]);
  // 만약에 applyStatus[role_id]가 '신청중'인데 한 번 더 클릭하게 되면 지원취소 API 호출
  // applyStatus는 다른화면에 들어가면 초기화됨 -> 전역상태관리 필요성 (redux)

  const handleApply = async (role_id, pre_cnt, want_cnt, role_name) => {
    if (!token) {
      alert('로그인 후 이용하세요.');
    }

    if (authorName === username) {
      alert('작성자는 지원할 수 없습니다.');
    }

    if (pre_cnt >= want_cnt) {
      alert('모집 완료되었습니다.');
    } else {
      const applyData = {
        board_id: parseInt(board_id),
        user_id,
        role_id,
        authorName,
      };

      console.log(applyData);
      try {
        const response = await axiosInstance.post('/applications', applyData);
        console.log('게시글 지원 성공', response);
        alert('지원이 완료되었습니다.');
        console.log(response);

        setApplyStatus({
          ...applyStatus,
          [role_id]: '신청중',
        });

        try {
          const notifyData = {
            receiverName: authorName,
            senderName: username,
            board_id,
            role_id,
            message: `${title} 의 ${role_name} 에 신청이 1건 있습니다.`,
            alarm_type: 'message',
            checked: true,
          };
          const notifyResponse = await axiosInstance.post(
            '/notify',
            notifyData
          );
          console.log(notifyData);
        } catch (error) {
          console.error('notify post 실패', error);
        }
      } catch (error) {
        console.error('게시글 지원 실패', error);
        console.log(error.response.data);
        if (
          error.response.data.message ===
          'Application Exception. 이미 지원한 게시글'
        ) {
          alert('이미 지원한 게시글입니다.');
          return;
        }
        if (error.response.data.status === '401') {
          console.log(error.response.data.msg);
          //TODO 리프레쉬토큰 리팩토링
          try {
            const retryResponse = await refreshTokenAndRetry(
              'post',
              `/applications`,
              applyData,
              {
                'X-AUTH-TOKEN': token,
              }
            );
            console.log('게시글 지원 성공 (재시도)');
            alert('지원이 완료되었습니다.');
            console.log(retryResponse);

            setApplyStatus({
              ...applyStatus,
              [role_id]: '신청중',
            });
          } catch (refreshError) {
            console.error('새로운 액세스 토큰 얻기 실패', refreshError);
          }
        }
      }
    }
  };

  const handleChangeCmt = (e) => {
    setComment(e.target.value);
  };

  const handleAddCmt = async (e) => {
    e.preventDefault();

    if (!token) {
      alert('로그인 후 이용하세요.');
    }

    const cmtInfo = {
      user_id,
      board_id,
      text: comment,
    };
    try {
      const response = await axiosInstance.post('/comments', cmtInfo);
      console.log(response);
      fetchBoard();
      alert('댓글이 작성되었습니다.');
      setComment('');
    } catch (error) {
      console.error('Error post comment', error);
      if (error.response.data.status === '401') {
        try {
          const retryResponse = await refreshTokenAndRetry(
            'post',
            `/comments`,
            {
              'X-AUTH-TOKEN': token,
            }
          );
          console.log('댓글 작성 (재시도)');
          console.log(retryResponse);
          alert('댓글이 작성되었습니다.');
        } catch (refreshError) {
          console.error('새로운 액세스 토큰 얻기 실패', refreshError);
        }
      }
    }
  };

  return (
    <Container>
      <TitleBox>
        <h1>{title}</h1>
        <h2>작성자: {authorName}</h2>
      </TitleBox>
      <Content>
        <div className="content_flex">
          <span>게시 날짜</span>
          <span>{formattedCreateAt}</span>
        </div>
        <div className="content_flex">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="18"
            height="18"
            viewBox="0 0 18 18"
            fill="none"
          >
            <path
              d="M7.65039 9.00039C7.65039 9.35843 7.79262 9.70181 8.0458 9.95498C8.29897 10.2082 8.64235 10.3504 9.00039 10.3504C9.35843 10.3504 9.70181 10.2082 9.95498 9.95498C10.2082 9.70181 10.3504 9.35843 10.3504 9.00039C10.3504 8.64235 10.2082 8.29897 9.95498 8.0458C9.70181 7.79262 9.35843 7.65039 9.00039 7.65039C8.64235 7.65039 8.29897 7.79262 8.0458 8.0458C7.79262 8.29897 7.65039 8.64235 7.65039 9.00039Z"
              stroke="black"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M15.75 9C13.95 12 11.7 13.5 9 13.5C6.3 13.5 4.05 12 2.25 9C4.05 6 6.3 4.5 9 4.5C11.7 4.5 13.95 6 15.75 9Z"
              stroke="black"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <span>{view_cnt}</span>
        </div>
        <div className="content_flex">
          <span>시작날짜</span>
          <span>{formattedPeriod}</span>
        </div>
        <div className="content_flex">
          <span>예상기간</span>
          <span>
            {monthDifference <= 0
              ? '1개월 미만'
              : `${monthDifference}개월 이상`}
          </span>
        </div>
        <div>
          <ClipBtn onClick={handleAddClip}>즐겨찾기</ClipBtn>
        </div>
      </Content>
      <Line></Line>
      <Section>
        <div className="section1">
          <div>
            <span>모집구분</span>
            <p>{type}</p>
          </div>
          <div>
            <span>사용기술</span>
            {tags && tags.map((tag, index) => <Tag key={index} tag={tag} />)}
          </div>
          <div>
            <span>진행방식</span>
            <p>{proceed_method}</p>
          </div>
          <div>
            <span>프로젝트 기간</span>
            <p>
              {formattedCreateAt} ~{' '}
              {monthDifference <= 0
                ? '1개월 미만'
                : `${monthDifference}개월 이상`}
            </p>
          </div>
        </div>
        <div className="section2">
          <div className="section2_title">프로젝트/스터디의 현재 인원</div>
          {roles &&
            roles.map((role) => (
              <div className="section2_content">
                <span>{role.name}</span>
                <div>
                  <p>{`${role.pre_cnt}/${role.want_cnt}`}</p>
                  <button
                    onClick={() =>
                      handleApply(
                        role.role_id,
                        role.pre_cnt,
                        role.want_cnt,
                        role.name
                      )
                    }
                    className={`${
                      role.pre_cnt >= role.want_cnt ? 'complete' : ''
                    } ${applyStatus[role.role_id] ? 'applying' : ''}`}
                  >
                    {role.pre_cnt >= role.want_cnt
                      ? '모집완료'
                      : applyStatus[role.role_id] || '신청하기'}
                  </button>
                </div>
              </div>
            ))}
        </div>
      </Section>
      <Section2>
        <div className="section2_title">
          <div>현재 인원</div>
          <div>3/9</div>
        </div>
        <ul className="section2_content">
          <li>
            <div>
              <img
                src="/profile/profile.png"
                width="150"
                height="150"
                alt="프로필"
              />
            </div>
            <div className="section2_content_text">
              <div>
                <span>닉네임</span>
                <p>김슬구</p>
              </div>
              <div>
                <span>역할</span>
                <p>프론트엔드</p>
              </div>
              <div>
                <span>기술 스택</span>
                <p>
                  <img src="/stack/js.png" alt="" />
                </p>
              </div>
              <div>
                <span>경력</span>
                <p>신입</p>
              </div>
            </div>
            <button>프로필 자세히</button>
          </li>
          <button className="next_btn">
            <img src="/icons/prev.png" alt="" />
          </button>
        </ul>
      </Section2>
      <Section3>
        <div className="section3_title">프로젝트 소개</div>
        <div className="section3_info">{text}</div>
        <div className="section3_title">댓글</div>
        <ul className="section3_content">
          {commentList &&
            commentList.map((comment) => (
              <li key={comment.user_id}>
                <img src="/profile/profile.png" alt="프로필 사진" />
                <div>
                  <p className="comment_user">{comment.username}</p>
                  <p className="comment_text">{comment.text}</p>
                </div>
              </li>
            ))}
        </ul>

        <form onSubmit={handleAddCmt}>
          <textarea
            className="section3_textarea"
            placeholder="간단한 궁금한 점을 물어보세요."
            onChange={handleChangeCmt}
            value={comment}
          />
          <button className="section3_btn">등록하기</button>
        </form>
      </Section3>
    </Container>
  );
}

const Container = styled.div`
  max-width: 1344px;
  margin: 2rem auto;
`;

const TitleBox = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;

  h1 {
    color: #000;
    font-size: 18px;
    font-style: normal;
    font-weight: 700;
    line-height: normal;
    margin-left: 8rem;
  }
  h2 {
    color: #000;
    font-size: 18px;
    font-style: normal;
    font-weight: 700;
    line-height: normal;
    margin-right: 8rem;
  }
`;

const Content = styled.div`
  margin: 3rem 8rem;
  display: flex;
  gap: 6rem;
  
  .content_flex {
    display: flex;
    align-items: center;
    justify-content = center;
    gap: 25px;
    color: #000;
    font-size: 1.2rem;
    font-style: normal;
    font-weight: 400;
    line-height: normal;
    span:first-child {
      color: #000;
      font-size: 1.2rem;
      font-style: normal;
      font-weight: 700;
      line-height: normal;
    }
  }
`;
const Line = styled.div`
  max-width: 1344px;
  margin: 0 7rem;
  height: 2px;
  background: #d9d9d9;
  margin-bottom: 24px;
`;

const Section = styled.div`
  border-top: 2px solid #d9d9d9;
  max-width: 1344px;
  margin: 2rem 7rem;
  height: 20rem;
  flex-shrink: 0;
  border-radius: 15px;
  display: flex;
  justify-content: center;
  gap: 20px;
  align-items: center;
  background: #dae9fc;
  .section1,
  .section2 {
    max-width: 577px;
    width: 27rem;
    height: 15rem;
    border-radius: 15px;
    margin: 0.2rem;
  }

  .section1 {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 50px;
    div {
      display: flex;
      align-items: center;
      span {
        display: inline-block;
        width: 150px;
        color: #1f7ceb;
        font-size: 18px;
        font-style: normal;
        font-weight: 400;
        line-height: normal;
      }

      p {
        color: #000;
        font-size: 20px;
        font-style: normal;
        font-weight: 400;
        line-height: normal;
      }
    }
  }

  .section2 {
    background: #fff;
    display: flex;
    flex-direction: column;

    .section2_title {
      padding-left: 20px;
      padding-top: 10px;
      padding-bottom: 20px;
      color: #000;
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: normal;
    }

    .section2_content {
      display: flex;
      justify-content: space-between;
      padding-left: 20px;
      padding-right: 20px;
      margin-bottom: 7px;
      span {
        font-size: 20px;
        font-style: normal;
        font-weight: 400;
        line-height: normal;
      }

      div {
        display: flex;
        gap: 15px;
        align-items: center;
        color: #000;
        font-size: 20px;
        font-style: normal;
        font-weight: 400;
        line-height: normal;
      }

      .complete {
        background: #1f7ceb;
        color: white;
      }

      .applying {
        color: #1f7ceb;
        border: 1px solid #1f7ceb;
        background: white;
        width: 5rem;
        height: 2.6rem;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      button {
        display: flex;
        padding: 10px;
        align-items: flex-start;
        gap: 10px;
        border-radius: 8px;
        background: #dae9fc;
        border: none;
        color: #000;
        font-size: 16px;
        font-style: normal;
        font-weight: 400;
        line-height: normal;
        cursor: pointer;
        width: 5rem;
        height: 2.6rem;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .active_btn {
        border-radius: 8px;
        background: #1f7ceb;
        color: white;
      }
    }
  }
`;

const Section2 = styled.div`
  margin: 3rem 7rem;

  .section2_title {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    div:first-child {
      color: #1f7ceb;
      font-size: 22px;
      font-style: normal;
      font-weight: 700;
      line-height: normal;
    }
    div:last-child {
      color: #000;
      font-size: 22px;
      font-style: normal;
      font-weight: 400;
      line-height: normal;
    }
  }

  .section2_content {
    position: relative;
    display: flex;
    gap: 15px;
    justify-content: start;
    padding: 0;

    li {
      list-style: none;
      width: 300.168px;
      height: 392.673px;
      flex-shrink: 0;
      border-radius: 50px;
      border: 2px solid #dae9fc;
      background: #fff;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 10px;
      .section2_content_text {
        div {
          display: flex;
          padding-bottom: 5px;
          gap: 15px;
          span {
            width: 100px;
            text-align: right;
            color: #858585;
            font-size: 14px;
            font-style: normal;
            font-weight: 400;
            line-height: normal;
          }
          p {
            color: #000;
            text-align: justify;
            font-size: 16px;
            font-style: normal;
            font-weight: 400;
            line-height: normal;
          }
        }
      }
      button {
        display: flex;
        width: 151px;
        height: 34px;
        padding: 10px 12px;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        flex-shrink: 0;
        border-radius: 8px;
        background: #1f7ceb;
        box-shadow: 0px 1px 2px 0px rgba(0, 0, 0, 0.1);
        color: #fff;
        font-size: 16px;
        font-style: normal;
        font-weight: 500;
        line-height: 22px;
        border: none;
        cursor: pointer;
      }
    }
    .next_btn {
      right: -100px;
      top: 50%;
      position: absolute;
      width: 50px;
      height: 50px;
      flex-shrink: 0;
      border-radius: 100%;
      border: none;
      cursor: pointer;
      background-color: #fff;
      transform: rotate(180deg);
    }
  }
`;

const Section3 = styled.div`
  margin: 2rem 7rem;
  .section3_title {
    color: #000;
    font-size: 22px;
    font-style: normal;
    font-weight: 700;
    line-height: normal;
    border-bottom: 2px solid #d9d9d9;
    padding-bottom: 10px;
  }

  .section3_info {
    height: 10rem;
    padding: 0;
    overflow-y: scroll;

    padding: 1rem;
  }

  .section3_content {
    height: 10rem;
    padding: 0;
    overflow-y: scroll;
    li {
      display: flex;
      margin: 0.3rem;
      margin-left: 0;
      align-items: center;

      img {
        width: 2rem;
        height: 2rem;
      }
      div {
        margin-left: 0.4rem;

        .comment_user {
          font-weight: 600;
          margin-bottom: 0.2rem;
        }
      }
    }
  }
  form {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    textarea {
      height: 7rem;
      width: 100%;
      margin: 2rem 7rem;
      flex-shrink: 0;
      border-radius: 30px;
      border: 2px solid #d9d9d9;
      padding: 20px;

      &::placeholder {
        color: #c2c2c2;
        font-size: 16px;
        font-style: normal;
        font-weight: 400;
        line-height: normal;
      }
    }
    button {
      display: flex;
      width: 151px;
      height: 34px;
      padding: 10px 12px;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      flex-shrink: 0;
      border-radius: 8px;
      background: #1f7ceb;
      box-shadow: 0px 1px 2px 0px rgba(0, 0, 0, 0.1);
      border: none;
      color: white;
      cursor: pointer;
    }
  }
`;

const ClipBtn = styled.button`
  background-color: #1f7ceb;
  color: white;
  border: none;
  padding: 0.6rem;
  border-radius: 0.5rem;

  &:hover {
    cursor: pointer;
  }
`;

const Tag = styled.div`
  background-size: cover;
  width: 1.5rem;
  height: 1.5rem;
  margin-right: 0.5rem;
  background-image: ${(props) => `url(/tag/${props.tag}.png)`};
`;
