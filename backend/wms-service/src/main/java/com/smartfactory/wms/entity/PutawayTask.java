package com.smartfactory.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_putaway_task")
public class PutawayTask extends BaseEntity {
    private String taskNo;
    private Long receiptItemId;
    private Long warehouseId;
    private Long materialId;
    private String materialName;
    private String batchNo;
    private BigDecimal qty;
    private Long fromLocationId;
    private Long toLocationId;
    private String toLocationCode;
    private String status;
    private String operatorName;
    private LocalDateTime finishTime;
}
