package com.smartfactory.mes.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PickItemDto {
    private Long materialId;
    private String materialCode;
    private String materialName;
    private BigDecimal qty;
}
