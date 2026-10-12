package com.smartfactory.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_receipt")
public class Receipt extends BaseEntity {
    private String receiptNo;
    private Long asnId;
    private Long warehouseId;
    private Long supplierId;
    private String supplierName;
    private String status;
    private Long operator;
    private String operatorName;
    private LocalDateTime receiveTime;
    private String remark;

    @TableField(exist = false)
    private List<ReceiptItem> items;
}
