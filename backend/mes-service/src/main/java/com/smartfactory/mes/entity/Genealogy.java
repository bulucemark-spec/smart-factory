package com.smartfactory.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("mes_genealogy")
public class Genealogy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workOrderId;
    private String parentBatch;
    private String childBatch;
    private Long materialId;
    private String materialCode;
    private BigDecimal qty;
}
