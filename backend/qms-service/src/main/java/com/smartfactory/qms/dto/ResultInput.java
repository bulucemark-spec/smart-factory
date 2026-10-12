package com.smartfactory.qms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResultInput {
    private Long itemId;
    private BigDecimal measuredValue;
    private String judge;   // 定性项 OK/NG
    private String remark;
}
