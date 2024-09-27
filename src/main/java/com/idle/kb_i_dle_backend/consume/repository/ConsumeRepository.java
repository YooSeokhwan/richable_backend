package com.idle.kb_i_dle_backend.consume.repository;

import com.idle.kb_i_dle_backend.consume.entity.OutcomeAverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ConsumeRepository extends JpaRepository<OutcomeAverage, Integer> {

    List<OutcomeAverage> findAll();
    List<OutcomeAverage> findByAgeGroupAndCategoryAndQuater(String category, String quater);

}

