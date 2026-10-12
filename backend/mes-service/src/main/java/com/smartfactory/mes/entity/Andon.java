package com.smartfactory.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mes_andon")
public class Andon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String callNo;
    private Long workstationId;
    private String type;
    private String level;
    private String status;
    private String callerName;
    private String responderName;
    private String description;
    private LocalDateTime callTime;
    private LocalDateTime respondTime;
    private LocalDateTime closeTime;
}
