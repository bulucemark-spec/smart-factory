package com.smartfactory.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("wms_outbound_item")
public class OutboundItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long outboundId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private BigDecimal qty;
    private BigDecimal pickedQty;
    private String locationCode;
}
