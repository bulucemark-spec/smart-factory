package com.smartfactory.qms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("qms_capa")
public class Capa {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String capaNo;
    private Long ncrId;
    private String rootCause;
    private String correctiveAction;
    private String preventiveAction;
    private Long owner;
    private String ownerName;
    private LocalDate dueDate;
    private String status;
    private String verifyResult;
    private String verifierName;
    private LocalDateTime createTime;
    private LocalDateTime closeTime;
}
