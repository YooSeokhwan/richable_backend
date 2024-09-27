package com.idle.kb_i_dle_backend.finance.repository;

import com.idle.kb_i_dle_backend.finance.entity.Spot;
import com.idle.kb_i_dle_backend.member.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long> {
    // deleteDate가 현재 날짜보다 이후이고, 카테고리가 특정 카테고리인 Spot의 가격 합계를 구함
    List<Spot> findByUidAndCategoryAndDeleteDateIsNull(User uid, String category);

    // 삭제되지 않은 현물 자산 전체 조회
    List<Spot> findByUidAndDeleteDateIsNull(User uid);

    // index 값 기준으로 가장 마지막 Spot 조회
    Optional<Spot> findTopByOrderByIndexDesc();

    // 특정 indev값의 정보 조회
    Optional<Spot> findByIndex(@Param("index")Integer index);

    void deleteByIndex(@Param("index")Integer index);

//    //젤위에꺼
//    List<Spot> findByUidAndDelete_dateAfter(long uid, Date currentDate);
//    //마지막
//    List<Spot> findByUidAndDelete_dateAfter(long uid, String category, Date currentDate);
}
