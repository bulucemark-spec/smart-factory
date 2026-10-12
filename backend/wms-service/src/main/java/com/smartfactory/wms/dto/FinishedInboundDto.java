package com.smartfactory.wms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinishedInboundDto {
    private String workOrderNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private BigDecimal qty;
    private Long warehouseId;
}
