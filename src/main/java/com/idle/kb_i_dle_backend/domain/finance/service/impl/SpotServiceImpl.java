package com.idle.kb_i_dle_backend.domain.finance.service.impl;

import com.idle.kb_i_dle_backend.domain.finance.dto.PriceSumDTO;
import com.idle.kb_i_dle_backend.domain.finance.dto.SpotDTO;
import com.idle.kb_i_dle_backend.domain.finance.entity.Spot;
import com.idle.kb_i_dle_backend.domain.finance.repository.SpotRepository;
import com.idle.kb_i_dle_backend.domain.finance.service.SpotService;
import com.idle.kb_i_dle_backend.domain.member.entity.Member;
import com.idle.kb_i_dle_backend.domain.member.service.MemberService;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpotServiceImpl implements SpotService {

    private final SpotRepository spotRepository;

    private final MemberService memberService;

    // 카테고리별 현물 자산 총합
    @Override
    public PriceSumDTO getTotalPriceByCategory(String category) throws Exception {
        Member member = memberService.findMemberByUid(1);
        List<Spot> spots = spotRepository.findByUidAndCategoryAndDeleteDateIsNull(member, category);

        if (spots.isEmpty()) {
            throw new NotFoundException("");
        }

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
        Member member = memberService.findMemberByUid(1);
        List<Spot> spots = spotRepository.findByUidAndDeleteDateIsNull(member);

        if (spots.isEmpty()) {
            throw new NotFoundException("");
        }

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
        Member member = memberService.findMemberByUid(1);
        List<Spot> spots = spotRepository.findByUidAndDeleteDateIsNull(member);

        if (spots.isEmpty()) {
            throw new NotFoundException("");
        }

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
        Member member = memberService.findMemberByUid(1);
        Spot savedSpot = spotRepository.save(SpotDTO.convertToEntity(member, spotDTO));

        return SpotDTO.convertToDTO(savedSpot);
    }

    // 현물 자산 수정
    @Transactional
    @Override
    public SpotDTO updateSpot(SpotDTO spotDTO) throws ParseException {
        Member member = memberService.findMemberByUid(1);

        // Spot 조회
        Spot isSpot = spotRepository.findByIndexAndDeleteDateIsNull(spotDTO.getIndex())
                .orElseThrow(() -> new IllegalArgumentException("Spot not found with id: " + spotDTO.getIndex()));

        // Spot의 소유자가 해당 User인지 확인
        if (!isSpot.getUid().equals(member)) {
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
        Member member = memberService.findMemberByUid(1);

        // Spot 조회
        Spot isSpot = spotRepository.findByIndexAndDeleteDateIsNull(index)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found with id: " + index));

        // Spot의 소유자가 해당 User인지 확인
        if (!isSpot.getUid().equals(member)) {
            throw new AccessDeniedException("You do not have permission to modify this spot.");
        }

        isSpot.setDeleteDate(new Date());

        Spot savedSpot = spotRepository.save(isSpot);
        return SpotDTO.convertToDTO(savedSpot);
    }

}