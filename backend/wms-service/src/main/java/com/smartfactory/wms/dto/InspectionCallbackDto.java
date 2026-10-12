package com.smartfactory.wms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InspectionCallbackDto {
    private String inspectionType;   // IQC / OQC
    private String sourceNo;         // 收货单号 / 工单号
    private Long materialId;
    private String batchNo;
    private Boolean passed;
    private BigDecimal qty;
}
