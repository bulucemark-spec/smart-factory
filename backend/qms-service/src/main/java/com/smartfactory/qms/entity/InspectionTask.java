package com.smartfactory.qms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qms_inspection_task")
public class InspectionTask extends BaseEntity {
    private String taskNo;
    private String inspectionType;
    private String sourceType;
    private String sourceNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private Long supplierId;
    private String supplierName;
    private Integer sampleSize;
    private String status;
    private Long inspector;
    private String inspectorName;
    private LocalDateTime inspectTime;
    private String conclusion;
}
