package com.idle.kb_i_dle_backend.member.controller;

import com.idle.kb_i_dle_backend.member.dto.AuthResultDTO;
import com.idle.kb_i_dle_backend.member.dto.CustomMember;
import com.idle.kb_i_dle_backend.member.dto.LoginDTO;
import com.idle.kb_i_dle_backend.member.dto.MemberDTO;
import com.idle.kb_i_dle_backend.member.dto.MemberJoinDTO;
import com.idle.kb_i_dle_backend.member.dto.MemberInfoDTO;
import com.idle.kb_i_dle_backend.member.service.MemberService;
import com.idle.kb_i_dle_backend.member.util.JwtProcessor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/member")
@PropertySource({"classpath:/application.properties"})
public class MemberController {

    private final AuthenticationManager authenticationManager;
    private final JwtProcessor jwtProcessor;
    private final MemberService memberService;

    public MemberController(AuthenticationManager authenticationManager, JwtProcessor jwtProcessor,
                            MemberService memberService) {
        this.authenticationManager = authenticationManager;
        this.jwtProcessor = jwtProcessor;
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        // Perform authentication
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getId(), loginDTO.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Assuming you have a method to fetch UserInfoDTO (e.g., from the authenticated user details)
            MemberInfoDTO userInfo = getUserInfoFromAuthentication(authentication);
            // Generate JWT token with uid
            String jwtToken = jwtProcessor.generateToken(userInfo.getId(), userInfo.getUid(), userInfo.getNickname());
            // Return the AuthResultDTO with token and user info
            return ResponseEntity.ok(new AuthResultDTO(jwtToken, userInfo));

        } catch (Exception e) {
            log.error("Authentication failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    // Helper method to retrieve user information from the authentication object
    private MemberInfoDTO getUserInfoFromAuthentication(Authentication authentication) {
        CustomMember customMember = (CustomMember) authentication.getPrincipal();
        MemberDTO member = customMember.getMember();  // Retrieve the MemberDTO object
        return new MemberInfoDTO(member.getUid(), member.getId(), member.getNickname(), member.getAuth().toString());
    }

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.redirect.uri}")
    private String redirectUri;

    @GetMapping("/naverlogin")
    public ResponseEntity<?> naverlogin(HttpServletRequest request) throws Exception {
        // 상태 토큰으로 사용할 랜덤 문자열 생성
        String state = generateState();

        // 세션 또는 별도의 저장 공간에 상태 토큰을 저장
        HttpSession session = request.getSession();
        session.setAttribute("naverState", state);
        // 디버깅을 위한 세션 상태 로깅
        log.error("생성된 상태 토큰: " + state);
        log.error("세션 ID: " + session.getId());

        String naverAuthUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                + "&state=" + state;
        log.error("naverAuthUrl: {}", naverAuthUrl);

        return ResponseEntity.ok(Map.of("redirectUrl", naverAuthUrl, "state", state));
    }

    public String generateState() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    @GetMapping("/naverCallback")
    public ResponseEntity<?> naverCallback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String state
    ) throws Exception {

        log.error("Received callback - code: {}, state: {}" + code, state);

        // 최종 인증 값인 접근 토큰을 발급
        String tokenUrl = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&state=" + state;
        String authtoken = getHttpResponse(tokenUrl);
        log.error("Token Response: {}", authtoken);
        log.error("tokenUrl: {}", tokenUrl);

        // JSON 파싱
        JSONObject tokenJson = new JSONObject(authtoken);
        String accessToken = tokenJson.getString("access_token");

        // 사용자 프로필 정보 조회
        JSONObject userProfile = getUserProfile(accessToken);
        log.info("User Profile: {}", userProfile.toString());

        // 여기서 userProfile을 사용하여 회원가입 또는 로그인 처리

        // Parse the user profile to extract necessary information
        JSONObject response = userProfile.getJSONObject("response");
        String email = response.getString("email");
        String birthyear = response.getString("birthyear");
        String gender = response.getString("gender");
        String nickname = response.getString("nickname");
        String naverId = response.getString("id");

        // Check if the email exists in your database
        boolean userExists = memberService.checkEmailExists(email);
        log.error("log for userExists"+userExists);

//        // 예: MemberDTO 생성 및 저장, JWT 토큰 생성 등
        if (userExists) {
            // User exists, proceed to authenticate
            MemberDTO member = memberService.findByEmail(email);
            log.error("log for member"+member);

            // Generate JWT token
            String jwtToken = jwtProcessor.generateToken(member.getId(), member.getUid(), member.getNickname());

            // Redirect to home page with token
            String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/")
                    .queryParam("token", jwtToken)
                    .toUriString();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();

        } else {
            // New user registration
            MemberJoinDTO memberJoinDTO = MemberJoinDTO.builder()
                    .id(email.split("@")[0])
                    .password(naverId)
                    .email(email)
                    .nickname(nickname)
                    .gender(gender.equals("M") ? 'M' : 'F')
                    .birth_year(Integer.valueOf(birthyear))
                    .auth("ROLE_MEMBER")
                    .agreementInfo(false)  // 기본값 설정
                    .agreementFinance(false)  // 기본값 설정
                    .build();

            memberService.MemberJoin(memberJoinDTO);

            MemberDTO member = memberService.findByEmail(email);
            String parid = member.getId();

            String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/user/terms")
                    .queryParam("id", parid).toUriString();
            log.error("redirectUrl"+redirectUrl);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        }
    }

    private String getHttpResponse(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private JSONObject getUserProfile(String accessToken) throws Exception {
        String profileUrl = "https://openapi.naver.com/v1/nid/me";
        URL url = new URL(profileUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = con.getResponseCode();
        BufferedReader br;
        if (responseCode == 200) {
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();

        return new JSONObject(response.toString());
    }

    @PostMapping("/register")
    public ResponseEntity<?> signup(@RequestBody MemberJoinDTO signupDTO) {
        try {
            if (signupDTO.isAgreementInfo() != true ) {
                signupDTO.setAgreementInfo(false);
            }
            if (signupDTO.isAgreementFinance() != true) {
                signupDTO.setAgreementFinance(false);}
            memberService.MemberJoin(signupDTO);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();  // Log the error for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the request.");
        }
    }

    @GetMapping("/checkDupl/{id}")
    public ResponseEntity<Map<String, Boolean>> checkDuplicateUsername(@PathVariable String id) {
        boolean exists = memberService.checkDupl(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/terms/{id}")
    public ResponseEntity<Map<String, Boolean>> updateUserAgreement(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> agreementData) {

        boolean info = agreementData.get("info");
        boolean finance = agreementData.get("finance");

        boolean success = memberService.checkAgree(info, finance, id);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/findid")
    public ResponseEntity<?> findId(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String id = memberService.findIdByEmail(email);
        if (id != null) {
            String verificationCode = memberService.generateAndSaveVerificationCode(email);
            System.out.println(verificationCode);
            log.info("verification code: " + verificationCode);
            // TODO: 이메일로 verificationCode 전송 로직 구현
            return ResponseEntity.ok(Map.of("message", "인증 코드가 이메일로 전송되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "해당 이메일로 등록된 아이디가 없습니다."));
        }
    }

    @PostMapping("/findid/auth")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");
        boolean isValid = memberService.verifyCode(email, code);
        if (isValid) {
            String id = memberService.findIdByEmail(email);
            return ResponseEntity.ok(Map.of("id", id));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "잘못된 인증 코드입니다."));
        }
    }

    @PostMapping("/find/pw/auth")
    public ResponseEntity<?> findPw(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String id = memberService.findIdByEmail(email);
        if (id != null) {
            String verificationCode = memberService.generateAndSaveVerificationCode(email);
            System.out.println(verificationCode);
            log.info("verification code: " + verificationCode);
            // TODO: 이메일로 verificationCode 전송 로직 구현
            return ResponseEntity.ok(Map.of("message", "인증 코드가 이메일로 전송되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "해당 이메일로 등록된 아이디가 없습니다."));
        }
    }

    @PostMapping("/find_pw_auth")
    public ResponseEntity<?> verifyCode2(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("code");
        boolean isValid = memberService.verifyCode(email, code);
        if (isValid) {
            String id = memberService.findIdByEmail(email);
            return ResponseEntity.ok(Map.of("id", id));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "잘못된 인증 코드입니다."));
        }
    }

    @PostMapping("/set/pw")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String id = payload.get("id");
        String newPassword = payload.get("newPassword");
        boolean success = memberService.resetPassword(id, newPassword);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 재설정되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "비밀번호 재설정 중 오류가 발생했습니다."));
        }
    }


}