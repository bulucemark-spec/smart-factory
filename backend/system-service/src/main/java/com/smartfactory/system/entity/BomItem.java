package com.smartfactory.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("mdm_bom_item")
public class BomItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bomId;
    private Long materialId;
    private BigDecimal quantity;
    private String unit;
    private Integer sequence;
    private BigDecimal lossRate;

    @TableField(exist = false)
    private String materialCode;
    @TableField(exist = false)
    private String materialName;
}
