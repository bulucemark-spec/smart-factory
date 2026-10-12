package com.smartfactory.mes.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mes_work_order")
public class WorkOrder extends BaseEntity {
    private String orderNo;
    private String type;
    private Long productMaterialId;
    private String productCode;
    private String productName;
    private Long bomId;
    private Long routingId;
    private Long workCenterId;
    private BigDecimal qtyPlanned;
    private BigDecimal qtyCompleted;
    private BigDecimal qtyDefect;
    private BigDecimal qtyScrap;
    private String status;
    private String finishedBatch;
    private LocalDateTime planStart;
    private LocalDateTime planEnd;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;
    private String remark;
}
