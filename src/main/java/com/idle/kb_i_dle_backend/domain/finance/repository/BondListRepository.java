package com.idle.kb_i_dle_backend.domain.finance.repository;

import com.idle.kb_i_dle_backend.domain.finance.entity.BondList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BondListRepository extends JpaRepository<BondList,Integer> {
    List<BondList> findTop5ByOrderByPriceDesc();

}
