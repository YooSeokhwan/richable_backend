package com.idle.kb_i_dle_backend.finance.controller;

import com.idle.kb_i_dle_backend.common.dto.SuccessResponseDTO;
import com.idle.kb_i_dle_backend.finance.dto.PriceSumDTO;
import com.idle.kb_i_dle_backend.finance.dto.SpotDTO;
import com.idle.kb_i_dle_backend.finance.entity.Spot;
import com.idle.kb_i_dle_backend.finance.service.SpotService;
import com.idle.kb_i_dle_backend.finance.service.SpotServiceImpl;
import com.idle.kb_i_dle_backend.member.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/finance")
@Slf4j
@RequiredArgsConstructor
public class SpotController {

    @Autowired
    private final SpotService spotService;  // SpotServiceImpl 대신 SpotService 인터페이스로 주입


    // 카테고리에 따른 총 가격 반환
    @GetMapping("/spot/category/sum")
    public ResponseEntity<SuccessResponseDTO> getTotalPriceByCategory(@RequestBody HashMap<String, String> map, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer uid = (Integer) session.getAttribute("uid");
        String category = map.get("category");
        Long totalPrice = spotService.getTotalPriceByCategory(uid, category);

        PriceSumDTO priceSum = new PriceSumDTO();
        priceSum.setProdCategory(category);
        priceSum.setAmount(totalPrice);

        // response를 감싸는 구조로 반환
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", priceSum);

        SuccessResponseDTO successResponse = new SuccessResponseDTO("true", responseData);
        return ResponseEntity.ok(successResponse);  // 응답을 ResponseEntity로 감싸서 반환
    }

    // 현물 자산 총 가격 반환
    @GetMapping("/spot/sum")
    public ResponseEntity<SuccessResponseDTO> getTotalPriceByCategory(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer uid = (Integer) session.getAttribute("uid");

        Long totalPrice = spotService.getTotalPrice(uid);

        PriceSumDTO priceSum = new PriceSumDTO();
        priceSum.setProdCategory("현물자산");
        priceSum.setAmount(totalPrice);

        // response를 감싸는 구조로 반환
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", priceSum);

        SuccessResponseDTO successResponse = new SuccessResponseDTO("true", responseData);
        return ResponseEntity.ok(successResponse);  // 응답을 ResponseEntity로 감싸서 반환
    }

    // 현물 자산 총 가격 반환
    @GetMapping("/spot/all")
    public ResponseEntity<SuccessResponseDTO> getTotalSpotList(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer uid = (Integer) session.getAttribute("uid");

        List<Spot> spots = spotService.getSpotList(uid);
        List<SpotDTO> spotList = new ArrayList<>();

        // 날짜 형식을 변환할 포맷 설정 (예: "yyyy-MM-dd")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (Spot s : spots) {
            SpotDTO spotDTO = new SpotDTO();
            spotDTO.setIndex(s.getIndex());
            spotDTO.setCategory(s.getCategory());
            spotDTO.setName(s.getName());
            spotDTO.setPrice(s.getPrice());
            String formattedAddDate = dateFormat.format(s.getAddDate());
            spotDTO.setAddDate(formattedAddDate);
            spotList.add(spotDTO);
        }


        // response를 감싸는 구조로 반환
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", spotList);

        SuccessResponseDTO successResponse = new SuccessResponseDTO("true", responseData);
        return ResponseEntity.ok(successResponse);  // 응답을 ResponseEntity로 감싸서 반환
    }

    // 새로운 Spot 추가
    @PostMapping("/spot/add")
    public ResponseEntity<SuccessResponseDTO> addSpot(@RequestBody Spot spot, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer uid = (Integer) session.getAttribute("uid");

        SpotDTO savedSpotDTO = spotService.addSpot(uid, spot);

        // response를 감싸는 구조로 반환
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", savedSpotDTO);

        SuccessResponseDTO successResponse = new SuccessResponseDTO("true", responseData);

        return ResponseEntity.ok(successResponse);  // 저장된 Spot 객체를 반환
    }

    // Spot 수정
    @PutMapping("/spot/update")
    public ResponseEntity<SuccessResponseDTO> updateSpot(@RequestBody Spot spot, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer uid = (Integer) session.getAttribute("uid");

        SpotDTO savedSpotDTO = spotService.updateSpot(uid, spot);

        // response를 감싸는 구조로 반환
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", savedSpotDTO);

        SuccessResponseDTO successResponse = new SuccessResponseDTO("true", responseData);

        return ResponseEntity.ok(successResponse);  // 저장된 Spot 객체를 반환
    }

    // Spot 삭제
    @DeleteMapping("/spot/delete/{index}")
    public ResponseEntity<SuccessResponseDTO> deleteSpot(@PathVariable("index") Integer index, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Integer uid = (Integer) session.getAttribute("uid");

        spotService.deleteSpotByUidAndIndex(uid, index);

        Map<String, Object> dataObject = new HashMap<>();
        dataObject.put("index", index);

        // response를 감싸는 구조로 반환
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", dataObject);

        SuccessResponseDTO successResponse = new SuccessResponseDTO("true", responseData);

        return ResponseEntity.ok(successResponse);  // 저장된 Spot 객체를 반환
    }
}
