package com.smartfactory.mes.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mes_workstation")
public class Workstation extends BaseEntity {
    private String code;
    private String name;
    private Long workCenterId;
    private String workCenterCode;
    private String productionLine;
    private Integer status;
}
