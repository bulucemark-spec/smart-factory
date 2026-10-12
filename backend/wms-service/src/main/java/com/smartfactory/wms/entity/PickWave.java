package com.smartfactory.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_pick_wave")
public class PickWave extends BaseEntity {
    private String waveNo;
    private String sourceType;
    private Long sourceId;
    private String sourceNo;
    private Long warehouseId;
    private String status;

    @TableField(exist = false)
    private List<PickTask> tasks;
}
