package com.smartfactory.mes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinishedInboundDto {
    private String orderNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private BigDecimal qty;
}
