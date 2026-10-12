package com.smartfactory.qms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartfactory.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("qms_ncr")
public class Ncr extends BaseEntity {
    private String ncrNo;
    private String sourceType;
    private String sourceNo;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private BigDecimal qty;
    private String defectDesc;
    private String level;
    private String status;
    private String disposition;
    private String mrbComment;
    private String creatorName;
    private LocalDateTime closeTime;
}
