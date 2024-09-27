package com.idle.kb_i_dle_backend.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpotDTO {
    private Integer index;
    private String category;
    private String name;
    private Long price;
    private String addDate;
}
