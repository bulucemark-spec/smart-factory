package com.smartfactory.qms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("qms_inspection_item")
public class InspectionItem {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long standardId;
    private String name;
    private String tool;
    private String standardValue;
    private BigDecimal upperLimit;
    private BigDecimal lowerLimit;
    private String unit;
    private Integer ctq;
    private Integer sequence;
}
