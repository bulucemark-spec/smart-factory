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
@TableName("wms_outbound")
public class Outbound extends BaseEntity {
    private String outboundNo;
    private String type;
    private String customer;
    private Long warehouseId;
    private String status;
    private String remark;
    private String operatorName;
    private LocalDateTime shipTime;

    @TableField(exist = false)
    private List<OutboundItem> items;
}
