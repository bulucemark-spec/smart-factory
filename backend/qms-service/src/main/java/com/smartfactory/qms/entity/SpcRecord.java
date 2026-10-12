package com.smartfactory.qms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("qms_spc_record")
public class SpcRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String characteristic;
    private String materialCode;
    private String workOrderNo;
    private BigDecimal value;
    private String sourceType;
    private LocalDateTime recordTime;
    private LocalDateTime createTime;
}
