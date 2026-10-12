package com.smartfactory.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mdm_routing")
public class Routing extends BaseEntity {
    private String code;
    private Long productMaterialId;
    private String version;
    private Integer status;
    private String remark;

    @TableField(exist = false)
    private List<RoutingOperation> operations;
}
