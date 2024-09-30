package com.idle.kb_i_dle_backend.finance.controller;

import com.idle.kb_i_dle_backend.common.dto.DataDTO;
import com.idle.kb_i_dle_backend.common.dto.ErrorResponseDTO;
import com.idle.kb_i_dle_backend.common.dto.ResponseDTO;
import com.idle.kb_i_dle_backend.common.dto.SuccessResponseDTO;
import com.idle.kb_i_dle_backend.finance.dto.PriceSumDTO;
import com.idle.kb_i_dle_backend.finance.dto.SpotDTO;
import com.idle.kb_i_dle_backend.finance.entity.UserSpot;
import com.idle.kb_i_dle_backend.finance.service.SpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> getTotalPriceByCategory(@RequestBody HashMap<String, String> map) {
        String category = map.get("category");
        try {
            ResponseDTO response = new ResponseDTO(true, new DataDTO(spotService.getTotalPriceByCategory(category)));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponseDTO response = new ErrorResponseDTO(false, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }



    // 현물 자산 총 가격 반환
    @GetMapping("/spot/sum")
    public ResponseEntity<?> getTotalPriceByCategory(){
        try {
            ResponseDTO response = new ResponseDTO(true, new DataDTO(spotService.getTotalPrice()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponseDTO response = new ErrorResponseDTO(false, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // 현물 자산 리스트 반환
    @GetMapping("/spot/all")
    public ResponseEntity<?> getTotalSpotList() {
        try {
            ResponseDTO response = new ResponseDTO(true, new DataDTO(spotService.getSpotList()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponseDTO response = new ErrorResponseDTO(false, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // 새로운 Spot 추가
    @PostMapping("/spot/add")
    public ResponseEntity<?> addSpot(@RequestBody UserSpot spot) {
        try {
            ResponseDTO response = new ResponseDTO(true, new DataDTO(spotService.addSpot(spot)));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponseDTO response = new ErrorResponseDTO(false, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Spot 수정
    @PutMapping("/spot/update")
    public ResponseEntity<?> updateSpot(@RequestBody UserSpot spot) {
        try {
            ResponseDTO response = new ResponseDTO(true, new DataDTO(spotService.updateSpot(spot)));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponseDTO response = new ErrorResponseDTO(false, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    // Spot 삭제
    @DeleteMapping("/spot/delete/{index}")
    public ResponseEntity<?> deleteSpot(@PathVariable("index") Integer index, HttpServletRequest request) {
        try {
            Map<String, Object> indexData = new HashMap<>();
            indexData.put("index", spotService.deleteSpotByUidAndIndex(index));
            ResponseDTO response = new ResponseDTO(true, new DataDTO(indexData));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ErrorResponseDTO response = new ErrorResponseDTO(false, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
