package com.smartfactory.qms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DisposeDto {
    private Long materialId;
    private String batchNo;
    private String disposition;
    private BigDecimal qty;
}
