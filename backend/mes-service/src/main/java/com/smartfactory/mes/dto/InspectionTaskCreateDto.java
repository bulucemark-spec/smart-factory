package com.smartfactory.mes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InspectionTaskCreateDto {
    private String inspectionType;
    private String sourceType;
    private String sourceNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private Long supplierId;
    private String supplierName;
    private BigDecimal qty;
}
