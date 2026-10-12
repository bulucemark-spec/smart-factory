package com.smartfactory.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mes_work_report")
public class WorkReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workOrderId;
    private String orderNo;
    private Integer operationSeq;
    private String operationName;
    private Long workstationId;
    private String workstationCode;
    private Long userId;
    private String userName;
    private BigDecimal goodQty;
    private BigDecimal defectQty;
    private BigDecimal scrapQty;
    private String defectCode;
    private LocalDateTime reportTime;
}
