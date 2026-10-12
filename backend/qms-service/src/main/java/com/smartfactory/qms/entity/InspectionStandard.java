package com.smartfactory.qms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qms_inspection_standard")
public class InspectionStandard extends BaseEntity {
    private String code;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Long supplierId;
    private String supplierName;
    private String inspectionType;
    private String aqlLevel;
    private Integer sampleSize;
    private Integer status;
    private String remark;

    @TableField(exist = false)
    private List<InspectionItem> items;
}
