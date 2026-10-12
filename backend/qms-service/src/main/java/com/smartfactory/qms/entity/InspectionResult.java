package com.smartfactory.qms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("qms_inspection_result")
public class InspectionResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long itemId;
    private String itemName;
    private BigDecimal measuredValue;
    private String judge;
    private String remark;
}
