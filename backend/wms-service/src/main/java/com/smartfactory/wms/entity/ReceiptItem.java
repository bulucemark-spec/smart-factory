package com.smartfactory.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("wms_receipt_item")
public class ReceiptItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long receiptId;
    private Long asnItemId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private LocalDate productionDate;
    private BigDecimal qty;
    private String status;
    private Long locationId;
}
