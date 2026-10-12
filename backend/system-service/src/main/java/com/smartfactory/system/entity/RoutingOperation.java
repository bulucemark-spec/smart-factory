package com.smartfactory.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("mdm_routing_operation")
public class RoutingOperation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long routingId;
    private Integer sequence;
    private String operationName;
    private Long workCenterId;
    private BigDecimal standardTime;
    private Integer isQcPoint;
    private Integer ctq;
    private String description;

    @TableField(exist = false)
    private String workCenterCode;
    @TableField(exist = false)
    private String workCenterName;
}
