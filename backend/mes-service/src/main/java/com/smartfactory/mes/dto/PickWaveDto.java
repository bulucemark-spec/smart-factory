package com.smartfactory.mes.dto;

import lombok.Data;

import java.util.List;

@Data
public class PickWaveDto {
    private Long sourceId;
    private String sourceNo;
    private Long warehouseId;
    private List<PickItemDto> items;
}
