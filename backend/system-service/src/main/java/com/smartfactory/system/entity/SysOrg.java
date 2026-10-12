package com.smartfactory.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends BaseEntity {
    private Long parentId;
    private String orgType;
    private String code;
    private String name;
    private String path;
    private Integer sort;
    private Integer status;
}
