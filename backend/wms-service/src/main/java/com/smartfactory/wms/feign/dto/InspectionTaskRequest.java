package com.smartfactory.wms.feign.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InspectionTaskRequest {
    private String inspectionType;   // IQC / OQC
    private String sourceType;       // RECEIPT / WORK_ORDER
    private String sourceNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private Long supplierId;
    private String supplierName;
    private BigDecimal qty;
}
