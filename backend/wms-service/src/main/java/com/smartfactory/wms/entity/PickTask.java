package com.smartfactory.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_pick_task")
public class PickTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long waveId;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private Long locationId;
    private String locationCode;
    private BigDecimal qty;
    private BigDecimal pickedQty;
    private String status;
    private String operatorName;
    private LocalDateTime pickTime;
}
