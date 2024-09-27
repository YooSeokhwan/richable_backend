//package com.idle.kb_i_dle_backend.member.entity;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import javax.persistence.*;
//
//@Getter
//@Setter
////@Entity
////@Table(name = "user_info")
//public class UserInfoEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private int uid;
//
//    @Column(length = 50, nullable = false)
//    private String nickname;
//
//    @Column(length = 100, nullable = false)
//    private String email;
//
//    @Column(length = 255)
//    private String profile;
//
//    @Column(name = "birth_year")
//    private String birthYear;
//
//    @Column(length = 1)
//    private String gender;
//
//    @Column(name = "is_certification", columnDefinition = "TINYINT(1)")
//    private boolean isCertification;
//
//    // API 관련 필드 추가
//    @Column(name = "api_bank", length = 255)
//    private String apiBank;
//
//    @Column(name = "api_stock_base", length = 255)
//    private String apiStockBase;
//
//    @Column(name = "api_stock_token", length = 255)
//    private String apiStockToken;
//
//    @Column(name = "api_stock_secret", length = 255)
//    private String apiStockSecret;
//
//    @Column(name = "api_stock_app", length = 255)
//    private String apiStockApp;
//
//    @Column(name = "api_coin_base", length = 255)
//    private String apiCoinBase;
//
//    @Column(name = "api_coin_secret", length = 255)
//    private String apiCoinSecret;
//
//    @Column(name = "api_coin_app", length = 255)
//    private String apiCoinApp;
//
//    // 기본 생성자 및 Getter, Setter는 Lombok에 의해 자동 생성됨
//}
