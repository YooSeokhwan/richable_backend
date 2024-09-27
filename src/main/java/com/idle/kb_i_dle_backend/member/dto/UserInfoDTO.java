package com.idle.kb_i_dle_backend.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
//새로 수정 9.26
@Data
@AllArgsConstructor
@Getter
@Setter
public class UserInfoDTO {
    private Integer uid;
    private String id;
    private String nickname;
    private String auth;
    private String email;      // 추가 필드
    private String img;        // 추가 필드
    private String birthYear;  // 추가 필드
    private String gender;     // 추가 필드
    private boolean certification;
    private UserApiDTO api;

    // 필요한 필드만 사용하는 생성자 (이 경우 uid, id, nickname, auth)

    public UserInfoDTO(Integer uid, String id, String nickname, String auth) {
        this.uid = uid;
        this.id = id;
        this.nickname = nickname;
        this.auth = auth;
        // 나머지 필드는 기본 값으로 설정
        this.email = null;
        this.img = null;
        this.birthYear = null;
        this.gender = null;
        this.certification = false;
        this.api = null;
    }
}