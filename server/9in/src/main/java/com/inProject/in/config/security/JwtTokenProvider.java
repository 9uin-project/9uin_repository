package com.inProject.in.config.security;

import com.inProject.in.domain.User.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor    //생성자로 의존성 주입받을 때 final 필드들은 이 어노테이션으로 주입 가능
public class JwtTokenProvider {
    private final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final UserDetailsService userDetailsService;
    private final RedisTemplate redisTemplate;
    @Value("${springboot.jwt.secret}")
    private String secretKey = "secretKey-for-authorization-jwtToken";
    private final long tokenValidMilliSecond = 1000L * 60 * 60;
    private final long refreshValidMilliSecond = tokenValidMilliSecond * 24;
    @PostConstruct        //해당 객체가 주입된 이후 수행되는 메서드 지정
    protected void init(){
        log.info("init ==> secret 키 초기화 시작");
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));  //secret key를 base64형식으로 인코딩한다.
        log.info("init ==> secret 키 초기화 완료");
    }


    public String createToken(String username, List<String> roles){
        log.info("JwtToken createToken ==> 토큰 생성 시작");
        Claims claims = Jwts.claims().setSubject(username);  //sub속성 값을 추가.
        claims.put("roles", roles);

        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)           //iat 속성에 값 추가. jwt가 발급된 시간.
                .setExpiration(new Date(now.getTime() + tokenValidMilliSecond))  //jwt의 만료시간
                .signWith(SignatureAlgorithm.HS256, secretKey)    //secret값 세팅. 암호화 알고리즘 적용.
                .compact();  //최종적으로 사용자에게 전달할 형태로 jwt를 컴팩트.

        log.info("JwtToken createToken ==> 토큰 생성 완료");
        return token;
    }

    public String createRefreshToken(String username){
        log.info("JwtToken create RefreshToken ==> refresh token 생성 시작");
        Claims claims = Jwts.claims().setSubject(username);
//        claims.put("roles" ,roles);

        Date now = new Date();
        String refreshToken = Jwts.builder()              //refresh 토큰에는 사용자 이름은 들어가지만 권한은 들어가지 않았음.
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshValidMilliSecond))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        log.info("JwtToken create refresh Token ==> refresh 토큰 생성 완료");
        return refreshToken;
        //DB에 실제로 refresh 토큰 저장해야함.
    }

    public Authentication getAuthentication(String token){   //인증이 성공하면 SecurityContextHolder에 저장할 authentication을 생성하는 역할.
        log.info("JwtToken getAuthentication ==> 토큰 인증 시작" );
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token){
        log.info("JwtToken getUsername ==> 회원 정보 찾기");
        String info = Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();         //토큰 생성 시 sub속성을 username으로 했기에 sub를 리턴.
        return info;
    }

    public String resolveToken(HttpServletRequest request){
        log.info("JwtToken resolveToken ==> http 헤더로부터 token 값 추출 시작");
        return request.getHeader("X-AUTH-TOKEN");    //헤더값으로 전달된 X-AUTH-TOKEN값을 가져와 리턴한다. 헤더 이름은 임의로 변경가능
    }

    public String resolveRefreshToken(HttpServletRequest request){
        log.info("JwtToken resolveRefreshToken ==> http 헤더로부터 refresh token 값 추출 시작");
        return request.getHeader("REFRESH-TOKEN");
    }

    public boolean validateToken(String token){
        log.info("JwtToken validateToken ==> 토큰 유효성 체크 시작");
        try{
            if(!Objects.isNull(redisTemplate.opsForValue().get(token))){   //블랙리스트에 등록된 token -> 로그아웃한 유저.
                log.info("logout user ==> 예외 발생");
                return false;
            }

            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build()  //jws = json web signature(서명)
                    .parseClaimsJws(token);  //header, payload 파싱해서 json 형태로 변환 -> 지정된 비밀키로 서명 검증.

            return !claims.getBody().getExpiration().before(new Date());
        } catch(Exception e){
            log.info("JwtToken validateToken ==> 예외 발생");
            return false;
        }
    }

    // refreshToken 토큰 검증
    // db에 저장되어 있는 token과 비교
    // db에 저장한다는 것이 jwt token을 사용한다는 강점을 상쇄시킨다.
    // db 보다는 redis를 사용하는 것이 더욱 좋다. (in-memory db기 때문에 조회속도가 빠르고 주기적으로 삭제하는 기능이 기본적으로 존재합니다.)
    public boolean validateRefreshToken(String refreshToken){
        log.info("JwtToken validateRefreshToken ==> refresh 토큰 유효성 체크 시작");
        try{
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(refreshToken);

            return !claims.getBody().getExpiration().before(new Date());
        }catch (Exception e){
            log.info("JwtToken validateRefreshToken ==> 예외 발생 : " + e.getMessage());
            return false;
        }
    }

    public Long getExpiration(String accessToken){
        log.info("JwtToken getExpiration ==> 토큰의 만료 시간 반환 시작");
        Date expire = Jwts.parserBuilder().setSigningKey(secretKey)
                .build().parseClaimsJws(accessToken).getBody().getExpiration();

        long now = new Date().getTime();

        return (expire.getTime() - now);
    }

}
