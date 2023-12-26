import React, { Fragment } from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../../modules/auth';

const Header = (props) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const token = useSelector((state) => state.auth.token);
  console.log(token);
  const username = useSelector((state) => state.auth.username);

  const alarmList = useSelector((state) => state.alarm.alarmList);

  const handleNavigation = (path) => {
    navigate(path);
  };

  const handleLogin = () => {
    if (token) {
      dispatch(logout());
    } else {
      navigate('/user/login');
    }
  };

  return (
    <>
      <Headers>
        <Container>
          <Logo
            onClick={() => handleNavigation('/')}
            src="/logo/logoHead.png"
            alt="로고"
          />
          <Title>
            <div onClick={() => handleNavigation('/project')}>프로젝트</div>
            <div onClick={() => handleNavigation('/study')}>스터디</div>
          </Title>
          <LogButtons>
            {token && (
              <button onClick={() => handleNavigation('/AddPost')}>
                글 작성하기
              </button>
            )}
            {token && (
              <button onClick={() => handleNavigation('/mypage')}>
                <img src="/profile/profile.png" alt="프로필 이미지" />
                <div>
                  <p>{username}님</p>
                </div>
              </button>
            )}
            {token && (
              <button onClick={() => handleNavigation('/mypage/recruit')}>
                <img src="/icons/vector.png" alt="알람 이미지" />
                {alarmList && alarmList.length > 0 ? (
                  <ActiveAlarm></ActiveAlarm>
                ) : (
                  ''
                )}
              </button>
            )}
            <div onClick={handleLogin} className="logout">
              {token ? '로그아웃' : '로그인'}
            </div>
          </LogButtons>
        </Container>
      </Headers>
    </>
  );
};

const Headers = styled.header`
  position: fixed;
  top: 0;
  left: 50%;
  width: 100%;
  transform: translate(-50%, 0%);
  height: 80px;
  color: black;
  z-index: 99;
  background-color: white;
`;

const Container = styled.div`
  width: 100%;
  max-width: 1260px;
  padding-left: 20px;
  padding-right: 20px;
  margin: auto;
  display: flex;
  position: relative;

  @media only screen and (min-width: 768px) and (max-width: 1325px) {
    max-width: 768px;
  }
`;

const Logo = styled.img`
  cursor: pointer;
  position: absolute;
  top: 15px;
  left: 10px;
  border-radius: 8px;
  height: 50px;
`;

const Title = styled.h1`
  display: flex;
  gap: 2rem;
  position: absolute;
  left: 140px;
  div {
    font-size: 22px;
    cursor: pointer;
  }
`;

const LogButtons = styled.div`
  position: absolute;
  right: 0;
  height: 80px;
  align-items: center;
  display: flex;
  gap: 1rem;

  button:first-child {
    background: #1f7ceb;
    width: 151px;
    height: 38px;
    border-radius: 8px;
    padding: 10px 12px 10px 12px;
    border: none;
    font-size: 18px;
    line-height: 22px;
    color: white;
    cursor: pointer;
  }

  button:nth-child(2) {
    background: #d9d9d9;
    width: 34px;
    height: 34px;
    border-radius: 100%;
    cursor: pointer;
    border: none;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-left: 10px;
    img {
      width: 34px;
      height: 34px;
    }

    &:hover {
      div {
        display: block;
      }
    }

    div {
      background-color: #dae9fc;
      position: absolute;

      bottom: 0;
      padding: 0.2rem 0.4rem;
      border-radius: 2rem;
      display: none;
      p {
        font-size: 1rem;
        font-weight: 600;
      }
    }
  }

  button:nth-child(3) {
    background: #d9d9d9;
    width: 34px;
    height: 34px;
    border-radius: 100%;
    cursor: pointer;
    border: none;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-right: 10px;
    img {
      width: 20px;
      height: 20px;
    }

    position: relative;
  }

  .logout {
    font-size: 18px;
    cursor: pointer;
  }
`;

const ActiveAlarm = styled.div`
  width: 10px;
  height: 10px;
  background-color: #eb3e63;
  border-radius: 100%;
  z-index: 1;
  position: absolute;
  right: 1px;
  bottom: 1px;
`;

export default Header;
