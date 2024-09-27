package com.idle.kb_i_dle_backend.finance.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.idle.kb_i_dle_backend.member.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.mail.Session;
import javax.persistence.*;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "spot",catalog = "asset") //name =table명 , catalog =  schema
public class Spot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`index`")
    private Integer index;


    @ManyToOne
    @JoinColumn(name = "uid") // foreign key 이름 설정
    private User uid;

    private String category;

    private String name;

    private Long price;

    private String prod_category;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "add_date")
    private Date addDate;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "delete_date")
    private Date deleteDate;

    // 엔티티가 처음 영속화될 때(addDate를 자동으로 설정)
    @PrePersist
    protected void onCreate() {
        this.addDate = new Date();  // 현재 시간을 자동으로 설정
    }

}
