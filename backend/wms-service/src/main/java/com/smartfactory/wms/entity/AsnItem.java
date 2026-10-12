package com.smartfactory.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("wms_asn_item")
public class AsnItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long asnId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private BigDecimal planQty;
    private BigDecimal receivedQty;
}
