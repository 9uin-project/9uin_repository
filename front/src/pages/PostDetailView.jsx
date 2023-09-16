import axios from 'axios';
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import useFetchData from '../ components/hooks/getPostList';

export default function PostDetail() {
  const { board_id } = useParams();

  const { data, loading, error } = useFetchData('/boards/' + board_id);

  const { title, type, proceed_method, period, roles, tags } = data;
  console.log(tags);

  const formattedPeriod = new Date(data.period).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });

  return (
    <Container>
      <h1>{title}</h1>
      <Content>
        <div className="content_flex">
          <span>게시 날짜</span>
          <span>{formattedPeriod}</span>
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
          <span>123</span>
        </div>
        <div className="content_flex">
          <span>시작날짜</span>
          <span>{period}</span>
        </div>
        <div className="content_flex">
          <span>예상기간</span>
          <span>6개월</span>
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
            <p>2023.04.30 ~ 6개월</p>
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
                  <button>
                    {role.pre_cnt >= role.want_cnt ? '모집완료' : '신청하기'}
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
        <div className="section3_content">{data.text}</div>
        <form>
          <textarea
            className="section3_textarea"
            placeholder="간단한 궁금한 점을 물어보세요."
          />
          <button className="section3_btn">등록하기</button>
        </form>
      </Section3>
    </Container>
  );
}

const Container = styled.div`
  width: 1344px;
  margin: 50px auto;
  height: 2000px;

  h1 {
    color: #000;
    font-size: 18px;
    font-style: normal;
    font-weight: 700;
    line-height: normal;
    margin-left: 100px;
  }
`;

const Content = styled.div`
  margin-left: 100px;
  display: flex;
  margin-top: 67px;
  margin-bottom: 24px;
  gap: 160px;
  .content_flex {
    display: flex;
    align-items: center;
    gap: 30px;
    color: #000;
    font-size: 20px;
    font-style: normal;
    font-weight: 400;
    line-height: normal;
    span:first-child {
      color: #000;
      font-size: 20px;
      font-style: normal;
      font-weight: 700;
      line-height: normal;
    }
  }
`;
const Line = styled.div`
  width: 1344.006px;
  height: 2px;
  background: #d9d9d9;
  margin-bottom: 24px;
`;

const Section = styled.div`
  width: 1344px;
  height: 384px;
  flex-shrink: 0;
  border-radius: 15px;
  display: flex;
  justify-content: center;
  gap: 20px;
  align-items: center;
  background: #dae9fc;
  .section1,
  .section2 {
    width: 577px;
    height: 350px;
    border-radius: 15px;
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
  margin-top: 50px;

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
  margin-top: 100px;

  .section3_title {
    color: #000;
    font-size: 22px;
    font-style: normal;
    font-weight: 700;
    line-height: normal;
    border-bottom: 2px solid #d9d9d9;
    padding-bottom: 10px;
  }

  .section3_content {
    height: 500px;
  }
  form {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    textarea {
      width: 1343px;
      height: 147px;
      flex-shrink: 0;
      border-radius: 30px;
      border: 2px solid #d9d9d9;

      &::placeholder {
        padding: 20px;
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

const Tag = styled.div`
  background-size: cover;
  width: 1.5rem;
  height: 1.5rem;
  margin-right: 0.5rem;
  background-image: ${(props) => `url(/tag/${props.tag}.png)`};
`;
