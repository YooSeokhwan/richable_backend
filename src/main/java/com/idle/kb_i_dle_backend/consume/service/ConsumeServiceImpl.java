package com.idle.kb_i_dle_backend.consume.service;

import com.idle.kb_i_dle_backend.consume.dto.*;
import com.idle.kb_i_dle_backend.consume.entity.OutcomeAverage;
import com.idle.kb_i_dle_backend.consume.entity.OutcomeUser;
import com.idle.kb_i_dle_backend.consume.repository.ConsumeRepository;
import com.idle.kb_i_dle_backend.consume.repository.OutcomeUserRepository;
import com.idle.kb_i_dle_backend.member.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumeServiceImpl implements ConsumeService {

    private final ConsumeRepository consumeRepository;
    private final OutcomeUserRepository outcomeUserRepository;


    @Override
    public List<OutcomeAverageDTO> getAll() {
        // 엔티티를 DTO로 변환하여 반환
        return consumeRepository.findAll()
                .stream()
                .map(this::convertToOutcomeAverageDTO)  // 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    // OutcomeAverage 엔티티를 OutcomeAverageDTO로 변환하는 메서드
    private OutcomeAverageDTO convertToOutcomeAverageDTO(OutcomeAverage outcomeAverage) {
        return new OutcomeAverageDTO(
                outcomeAverage.getIndex(),
                outcomeAverage.getAgeGroup(),  // getter 사용
                outcomeAverage.getCategory(),  // getter 사용
                outcomeAverage.getOutcome(),
                outcomeAverage.getHouseholdSize(),
                outcomeAverage.getQuater()  // getter 사용
        );
    }

    @Override
    public List<OutcomeUserDTO> getAllUser() {
        return outcomeUserRepository.findAll()
                .stream()
                .map(this::convertToOutcomeUserDTO)  // 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    @Override
    public List<CategorySumDTO> getCategorySum(int uid, int year, int month) {
        return outcomeUserRepository.findCategorySumByUidAndYearAndMonth(uid, year, month);
    }

    private OutcomeUserDTO convertToOutcomeUserDTO(OutcomeUser outcomeUser) {
        return new OutcomeUserDTO(
                outcomeUser.getIndex(),
                outcomeUser.getUid(),
                outcomeUser.getCategory(),
                outcomeUser.getDate(),
                outcomeUser.getAmount(),
                outcomeUser.getDescript(),
                outcomeUser.getMemo()
        );
    }
    @Override
    public ResponseCategorySumListDTO findCategorySum(Integer year, Integer month) {
        List<CategorySumDTO> categorySumDTOS = outcomeUserRepository.findCategorySumByUidAndYearAndMonth(1 , year, month);
        Long sum = categorySumDTOS.stream().mapToLong(CategorySumDTO::getSum).sum();
        return new ResponseCategorySumListDTO(categorySumDTOS, sum);
    }

    @Override
    public MonthConsumeDTO findMonthConsume(Integer year, Integer month) {
        List<OutcomeUser> consumes = outcomeUserRepository.findAmountAllByUidAndYearAndMonth(1, year, month);
        List<Long> dailyAmount = new ArrayList<>(Collections.nCopies(31, 0L));
        for(OutcomeUser consume : consumes) {
            Date date = consume.getDate();
            int day = date.getDate();
            System.out.println(date.toString() + " " + consume.getAmount());
            dailyAmount.set(day -1 , dailyAmount.get(day -1 ) + consume.getAmount()) ;
        }

        // 누적합 계산
        long cumulativeSum = 0L;
        for (int i = 0; i < dailyAmount.size(); i++) {
            cumulativeSum += dailyAmount.get(i);  // 이전까지의 합을 더함
            dailyAmount.set(i, cumulativeSum);    // 누적합을 다시 리스트에 저장
        }

        System.out.println(consumes.toString());
        MonthConsumeDTO monthConsumeDTO = new MonthConsumeDTO(month,year,dailyAmount);

        return monthConsumeDTO;
    }

        @Override
        public List<AvgCategorySumDTO> findCompareWithAvg(Integer uid, String category, Integer year, Integer month) {
        //user_info repository에서 uid를 주고, 나이를 알아와야
        int birthYear = outcomeUserRepository.findBirthYearByUid(uid);
        int age = year - birthYear;
        //year랑 month로  => STring 값인 "2024년 1분기"
        String quater = getQuaterString(year, month);

        //해당 카테고리의 평균 소비
        //=> avgCategorySumDTO에 넣어
            List<OutcomeAverage> avgList = consumeRepository.findByAgeGroupAndCategoryAndQuater(category, quater);
            List<AvgCategorySumDTO> avgCategorySumDTOs = avgList.stream()
                    .map(avg -> new AvgCategorySumDTO(avg.getCategory(), avg.getOutcome()))
                    .collect(Collectors.toList());

            //유저의 해당 년도 해당 달의 해당카테고리의 소비
        //repo에서 가져와서
        //=> avgcategorysumDTO에 넣어야
    }
}
