package com.smartfactory.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mdm_work_center")
public class WorkCenter extends BaseEntity {
    private String code;
    private String name;
    private String workshop;
    private Long orgId;
    private BigDecimal capacityPerDay;
    private Integer status;
}
