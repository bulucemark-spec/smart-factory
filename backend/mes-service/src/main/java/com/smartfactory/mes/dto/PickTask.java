package com.smartfactory.mes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PickTask {
    private Long id;
    private Long waveId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private String locationCode;
    private BigDecimal qty;
    private String status;
}
