package com.smartfactory.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mdm_supplier")
public class Supplier extends BaseEntity {
    private String code;
    private String name;
    private String contactPerson;
    private String phone;
    private String address;
    private String grade;
    private BigDecimal qualifiedRate;
    private Integer ppm;
    private Integer status;
}
