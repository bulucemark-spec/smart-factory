package com.smartfactory.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mes_material_txn")
public class MaterialTxn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workOrderId;
    private String orderNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private BigDecimal qty;
    private String type;
    private LocalDateTime createTime;
}
