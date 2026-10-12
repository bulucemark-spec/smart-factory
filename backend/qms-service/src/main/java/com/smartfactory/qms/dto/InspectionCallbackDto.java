package com.smartfactory.qms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InspectionCallbackDto {
    private String inspectionType;
    private String sourceNo;
    private Long materialId;
    private String batchNo;
    private Boolean passed;
    private BigDecimal qty;
}
