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
@TableName("wms_asn")
public class Asn extends BaseEntity {
    private String asnNo;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private LocalDateTime expectArrivalTime;
    private String status;
    private String remark;

    @TableField(exist = false)
    private List<AsnItem> items;
}
