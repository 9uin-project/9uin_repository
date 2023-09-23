import React, { useState } from 'react';
import styled from 'styled-components';
import { useNavigate, useParams } from 'react-router-dom';
import useFetchData from '../../ components/hooks/getPostList';
import axios from 'axios';
import Modal from '../../ components/Mypage/MypageModal';

export default function RecruitStatusView() {
  const { data: postList, Loading, error } = useFetchData('/boards');
  const navigate = useNavigate();

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [postToDelete, setPostToDelete] = useState(null);

  const closeModal = () => {
    setShowDeleteModal(false);
  };

  const handleDelete = async (board_id) => {
    alert('게시글이 삭제되었습니다');
    try {
      const response = await axios.delete(
        `http://1.246.104.170:8080/boards/${board_id}`,
        {
          headers: {
            'X-AUTH-TOKEN': localStorage.getItem('token'),
          },
        }
      );
      console.log('글 삭제 성공');
      setShowDeleteModal(false);
    } catch (error) {
      console.error('글 삭제 실패', error);
      console.log(error.response.data);
      if (error.response.data.msg == '인증이 실패했습니다.') {
        console.log(error.response.data.msg);
        const refreshData = {
          refreshToken: localStorage.getItem('refreshToken'),
        };
        try {
          const refreshResponse = await axios.post(
            'http://1.246.104.170:8080/sign/reissue',
            refreshData
          );

          // 새로운 액세스 토큰 저장
          const newAccessToken = refreshResponse.data.accessToken;
          localStorage.setItem('token', newAccessToken);

          // 새로운 액세스 토큰을 사용하여 원래의 요청 다시 보내기
          const retryResponse = await axios.post(
            `http://1.246.104.170:8080/boards/${board_id}`,
            {
              headers: {
                'X-AUTH-TOKEN': newAccessToken,
              },
            }
          );
          console.log('글 삭제 성공 (재시도)');
        } catch (refreshError) {
          console.error('새로운 액세스 토큰 얻기 실패', refreshError);
        }
      }
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
            <TitleCount>1</TitleCount>
          </Title>
          {postList
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
            <TitleCount>1</TitleCount>
          </Title>
          {postList
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
      <NewBox>
        <LeftBox>
          <Icon />
          <TextContainer>
            <Line1>
              [프로젝트] 해커톤 모집합니다. (UXUI/프론트엔드/기획자/백엔드)
              열심히 하실분만...의 디자이너 신청이 1건 있습니다.
            </Line1>
            <Line2>신청자: 공대생23</Line2>
          </TextContainer>
        </LeftBox>
        <RightBox>
          <Button1>프로필 자세히</Button1>
          <Button2>승낙하기</Button2>
        </RightBox>
      </NewBox>
    </Container>
  );
}

const Container = styled.section`
  padding: 4rem;
`;

const MainTitle = styled.span`
  font-size: 0.8rem;
  margin-left: 2.5rem;
`;

const MyBox = styled.div`
  display: flex;
  justify-content: center;
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
`;

const NewBox = styled.div`
  border: 2px solid #d2e2ec;
  display: flex;
  justify-content: space-between;
  width: 93%;
  margin: auto;
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
