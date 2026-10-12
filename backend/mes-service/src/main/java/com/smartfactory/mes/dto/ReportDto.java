package com.smartfactory.mes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportDto {
    private Long workOrderId;
    private Long workstationId;
    private Integer operationSeq;
    private String operationName;
    private BigDecimal goodQty;
    private BigDecimal defectQty;
    private BigDecimal scrapQty;
    private String defectCode;
}
