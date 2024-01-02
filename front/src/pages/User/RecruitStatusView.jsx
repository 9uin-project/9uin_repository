import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import { useNavigate, useParams } from 'react-router-dom';
import useFetchData from '../../ components/hooks/getPostList';
import axios from 'axios';
import Modal from '../../ components/Mypage/MypageModal';
import { refreshTokenAndRetry } from '../../api/user';
import { createAxiosInstance } from '../../api/instance';
import { useSelector } from 'react-redux';

export default function RecruitStatusView() {
  const username = useSelector((state) => state.auth.username);

  const [postList, setPostList] = useState([]);

  const navigate = useNavigate();

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [postToDelete, setPostToDelete] = useState(null);

  const alarmList = useSelector((state) => state.alarm.alarmList);
  console.log(alarmList);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const params = {
          username,
        };
        const axiosInstance = createAxiosInstance(null, params);
        const response = await axiosInstance.get('/boards');
        setPostList(response.data.content);
      } catch (error) {
        console.error('게시글 리스트 Get 실패');
      }
    };

    fetchData();
  }, []);

  const closeModal = () => {
    setShowDeleteModal(false);
  };

  const handleDelete = async (board_id) => {
    const axiosInstance = createAxiosInstance(localStorage.getItem('token'));

    try {
      const response = await axiosInstance.delete(`/boards/${board_id}`);
      console.log('글 삭제 성공');
      alert('게시글이 삭제되었습니다');
      navigate('/mypage');
      setShowDeleteModal(false);
    } catch (error) {
      console.error('글 삭제 실패', error);
      console.log(error.response.data);
      if (error.response.data.status === '401') {
        console.log(error.response.data.msg);

        //TODO 리프레쉬토큰
        try {
          const retryResponse = await refreshTokenAndRetry(
            'delete',
            `/boards/${board_id}`,
            null,
            {
              'X-AUTH-TOKEN': localStorage.getItem('token'),
            }
          );
          console.log(retryResponse);
        } catch (retryError) {
          console.log(retryError);
        }
      }
    }
  };

  const handleAccept = async (index) => {
    const axiosInstance = createAxiosInstance(localStorage.getItem('token'));
    const applicationsInfo = {
      board_id: alarmList[index].board_id,
      role_id: alarmList[index].role_id,
      authorName: alarmList[index].receiverName,
    };

    try {
      const response = await axiosInstance.post(
        '/applications/accept',
        applicationsInfo
      );
      console.log(response);

      try {
        const notification_id = alarmList[index].id;
        const notifyAxios = createAxiosInstance(
          localStorage.getItem('token'),
          notification_id
        );
        const notifyRes = await notifyAxios.put('/notify');
        console.log(notifyRes);
      } catch (error) {
        console.error('notify post error', error);
      }
    } catch (error) {
      console.error('지원 수락실패', error);
      alert('수락하기를 실패하였습니다.');
    }
  };

  return (
    <Container>
      {showDeleteModal && (
        <Modal
          isOpen={showDeleteModal}
          onClose={closeModal}
          titleContent={<p>게시물을 삭제하시겠습니까?</p>}
          bodyContent={
            <ModalBtn onClick={() => handleDelete(postToDelete)}>확인</ModalBtn>
          }
        />
      )}
      <MainTitle>내가 운영중인</MainTitle>
      <MyBox>
        <MyRecruit>
          <Title>
            <span>프로젝트</span>
            <TitleCount>
              {postList &&
                postList.filter((post) => post.type === '프로젝트').length}
            </TitleCount>
          </Title>
          {postList &&
            postList
              .filter((post) => post.type === '프로젝트')
              .map((post) => (
                <ContentContainer>
                  <Truncate
                    onClick={() => navigate(`/postDetail/${post.board_id}`)}
                  >
                    {post.title}
                  </Truncate>
                  <Options>
                    <OptionItem
                      onClick={() => navigate(`/modifyPost/${post.board_id}`)}
                    >
                      수정
                    </OptionItem>
                    <OptionItem
                      onClick={() => {
                        setPostToDelete(post.board_id);
                        setShowDeleteModal(true);
                      }}
                    >
                      삭제
                    </OptionItem>
                  </Options>
                </ContentContainer>
              ))}
          <ContentContainer></ContentContainer>
        </MyRecruit>
        <MyRecruit>
          <Title>
            {' '}
            <span>스터디</span>
            <TitleCount>
              {' '}
              {postList &&
                postList.filter((post) => post.type === '스터디').length}
            </TitleCount>
          </Title>
          {postList &&
            postList
              .filter((post) => post.type === '스터디')
              .map((post) => (
                <ContentContainer>
                  <Truncate
                    onClick={() => navigate(`/postDetail/${post.board_id}`)}
                  >
                    {post.title}
                  </Truncate>
                  <Options>
                    <OptionItem
                      onClick={() => navigate(`/modifyPost/${post.board_id}`)}
                    >
                      수정
                    </OptionItem>
                    <OptionItem
                      onClick={() => {
                        setPostToDelete(post.board_id);
                        setShowDeleteModal(true);
                      }}
                    >
                      삭제
                    </OptionItem>
                  </Options>
                </ContentContainer>
              ))}
        </MyRecruit>
      </MyBox>
      <MainTitle>신청 알림</MainTitle>
      {alarmList &&
        alarmList.map((alarm, index) => (
          <NewBox key={alarm.id}>
            <LeftBox>
              <Icon />

              <TextContainer>
                <Line1>
                  [{alarm.board_type}] {alarm.message}
                </Line1>
                <Line2>신청자: {alarm.senderName}</Line2>
              </TextContainer>
            </LeftBox>
            <RightBox>
              <Button1>프로필 자세히</Button1>
              <Button2 onClick={() => handleAccept(index)}>승낙하기</Button2>
            </RightBox>
          </NewBox>
        ))}
    </Container>
  );
}

const Container = styled.section`
  padding: 4rem;
`;

const MainTitle = styled.span`
  font-size: 0.8rem;
  margin-left: 2.5rem;
  font-weight: 600;
`;

const MyBox = styled.div`
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
`;

const MyRecruit = styled.div`
  background-color: #dae9fc;
  margin: 1rem;
  width: 45%;
  height: 12rem;
  border-radius: 1rem;
`;

const Title = styled.div`
  display: flex;
  justify-content: center;
  margin: 1rem;
  font-size: 1.1rem;
`;

const TitleCount = styled.span`
  margin-left: 0.5rem;
`;
const ContentContainer = styled.div`
  display: flex;
  justify-content: space-between;
`;

const Truncate = styled.span`
  margin-left: 1rem;
  font-size: 0.8rem;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 70%;

  &:hover {
    cursor: pointer;
  }
`;

const Options = styled.div`
  font-size: 0.8rem;
  margin-right: 1.5rem;
`;

const OptionItem = styled.span`
  position: relative;

  &:not(:last-child)::after {
    content: ' | ';
    margin-left: 0.5rem;
  }

  &:hover {
    cursor: pointer;
  }
`;

const NewBox = styled.div`
  border: 2px solid #d2e2ec;
  display: flex;
  justify-content: space-between;
  width: 93%;
  margin: auto;
  margin-top: 0.8rem;
`;

const LeftBox = styled.div`
  display: flex;
  align-items: center;
`;

const Icon = styled.div`
  background: url('/icons/click.png');
  background-size: cover;
  width: 2.5rem;
  height: 2.5rem;
  margin: 0.5rem;
`;

const TextContainer = styled.div`
  padding: 0.5rem;
  margin: 1rem;
`;

const Line1 = styled.div`
  font-size: 0.8rem;
  margin-bottom: 0.5rem;
`;

const Line2 = styled.div`
  font-size: 0.7rem;
`;

const RightBox = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
`;

const Button1 = styled.button`
  background: none;
  padding: 1.5rem 0.7rem;
  border: 0.1rem solid #1f7ceb;
  color: #1f7ceb;
  font-size: 0.7rem;
  margin: 0.3rem;
  width: 5.5rem;
  border-radius: 0.5rem;

  &:hover {
    cursor: pointer;
  }
`;

const Button2 = styled.button`
  background: none;
  padding: 1.5rem 0.7rem;
  background-color: #1f7ceb;
  color: white;
  font-size: 0.7rem;
  margin: 0.3rem;
  width: 5.5rem;
  border: none;
  border-radius: 0.5rem;

  &:hover {
    cursor: pointer;
  }
`;

const ModalBtn = styled.button`
  background: white;
  padding: 0.9rem 0.7rem;
  border: 0.1rem solid #1f7ceb;
  color: #1f7ceb;
  font-size: 0.8rem;
  margin: 0.3rem;
  width: 3rem;
  border-radius: 0.5rem;
`;
