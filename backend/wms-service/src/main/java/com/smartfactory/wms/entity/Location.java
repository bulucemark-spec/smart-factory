package com.smartfactory.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_location")
public class Location extends BaseEntity {
    private Long warehouseId;
    private Long zoneId;
    private String aisle;
    private String code;
    private Integer x;
    private Integer y;
    private Integer layer;
    private BigDecimal capacity;
    private String locationType;
    private Integer status;
}
