package com.smartfactory.wms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DisposeDto {
    private Long materialId;
    private String batchNo;
    private String disposition;   // RETURN/SORT/REWORK/CONCESSION/SCRAP
    private BigDecimal qty;
}
