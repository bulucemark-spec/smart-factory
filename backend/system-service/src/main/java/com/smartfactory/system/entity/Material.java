package com.smartfactory.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mdm_material")
public class Material extends BaseEntity {
    private String code;
    private String name;
    private String category;
    private String specification;
    private String unit;
    private String materialType;
    private Integer batchManaged;
    private Integer shelfLifeDays;
    private String abcClass;
    private BigDecimal weight;
    private Integer status;
}
