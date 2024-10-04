package com.idle.kb_i_dle_backend.domain.finance.service.impl;

import com.idle.kb_i_dle_backend.domain.finance.dto.PriceSumDTO;
import com.idle.kb_i_dle_backend.domain.finance.dto.SpotDTO;
import com.idle.kb_i_dle_backend.domain.finance.entity.Spot;
import com.idle.kb_i_dle_backend.domain.finance.repository.SpotRepository;
import com.idle.kb_i_dle_backend.domain.finance.service.SpotService;
import com.idle.kb_i_dle_backend.domain.member.entity.Member;
import com.idle.kb_i_dle_backend.domain.member.repository.MemberRepository;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SpotServiceImpl implements SpotService {

    @Autowired
    private SpotRepository spotRepository;

    @Autowired
    private MemberRepository memberRepository;

    // 카테고리별 현물 자산 총합
    @Override
    public PriceSumDTO getTotalPriceByCategory(String category) throws Exception{
        Member tempUser = memberRepository.findByUid(1).orElseThrow();

        String result = category.equals("car") ? "자동차" :
                        category.equals("elec") ? "전자기기" :
                        category.equals("brand") ? "브랜드" :
                        category.equals("luxury") ? "명품" :
                        "기타";


        List<Spot> spots = spotRepository.findByUidAndCategoryAndDeleteDateIsNull(tempUser, result);

        if (spots.isEmpty()) throw new NotFoundException("");

        PriceSumDTO priceSum = new PriceSumDTO(
                category,
                spots.stream()
                        .mapToLong(Spot::getPrice)
                        .sum());

        return priceSum;
    }

    // 전체 현물 자산 총합
    @Override
    public PriceSumDTO getTotalPrice() throws Exception {
        Member tempUser = memberRepository.findByUid(1).orElseThrow();
        List<Spot> spots = spotRepository.findByUidAndDeleteDateIsNull(tempUser);

        if (spots.isEmpty()) throw new NotFoundException("");

        PriceSumDTO priceSum = new PriceSumDTO(
                "현물자산",
                spots.stream()
                        .mapToLong(Spot::getPrice)
                        .sum());

        return priceSum;
    }

    // 현물 자산 목록 전체 조회
    @Override
    public List<SpotDTO> getSpotList() throws Exception {
        Member tempUser = memberRepository.findByUid(1).orElseThrow();
        List<Spot> spots = spotRepository.findByUidAndDeleteDateIsNull(tempUser);

        if (spots.isEmpty()) throw new NotFoundException("");

        List<SpotDTO> spotList = new ArrayList<>();
        for (Spot s : spots) {
            SpotDTO spotDTO = SpotDTO.convertToDTO(s);
            spotList.add(spotDTO);
        }

        return spotList;
    }

    // 현물 자산 추가
    @Override
    public SpotDTO addSpot(SpotDTO spotDTO) throws ParseException {
        Member tempUser = memberRepository.findByUid(1).orElseThrow();
        Spot savedSpot = spotRepository.save(SpotDTO.convertToEntity(tempUser, spotDTO));

        return SpotDTO.convertToDTO(savedSpot);
    }

    // 현물 자산 수정
    @Transactional
    @Override
    public SpotDTO updateSpot(SpotDTO spotDTO) throws ParseException {
        Member tempUser = memberRepository.findByUid(1).orElseThrow();

        // Spot 조회
        Spot isSpot = spotRepository.findByIndexAndDeleteDateIsNull(spotDTO.getIndex())
                .orElseThrow(() -> new IllegalArgumentException("Spot not found with id: " + spotDTO.getIndex()));

        // User 조회 (User 객체가 없을 경우 예외 처리)
        Member isUser = memberRepository.findByUid(tempUser.getUid())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + tempUser.getUid()));

        // Spot의 소유자가 해당 User인지 확인
        if (!isSpot.getUid().equals(isUser)) {
            throw new AccessDeniedException("You do not have permission to modify this spot.");
        }

        isSpot.setName(spotDTO.getName());
        isSpot.setPrice(spotDTO.getPrice());

        Spot savedSpot = spotRepository.save(isSpot);
        return SpotDTO.convertToDTO(savedSpot);
    }

    // 특정 유저와 index에 해당하는 Spot 삭제
    @Transactional
    @Override
    public SpotDTO deleteSpot(Integer index) {
        Member tempUser = memberRepository.findByUid(1).orElseThrow();

        // Spot 조회
        Spot isSpot = spotRepository.findByIndexAndDeleteDateIsNull(index)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found with id: " + index));

        // User 조회 (User 객체가 없을 경우 예외 처리)
        Member isUser = memberRepository.findByUid(tempUser.getUid())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + tempUser.getUid()));

        // Spot의 소유자가 해당 User인지 확인
        if (!isSpot.getUid().equals(isUser)) {
            throw new AccessDeniedException("You do not have permission to modify this spot.");
        }

        isSpot.setDeleteDate(new Date());

        Spot savedSpot = spotRepository.save(isSpot);
        return SpotDTO.convertToDTO(savedSpot);
    }

}