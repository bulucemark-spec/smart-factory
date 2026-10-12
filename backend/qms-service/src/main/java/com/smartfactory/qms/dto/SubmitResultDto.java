package com.smartfactory.qms.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitResultDto {
    private Long taskId;
    private List<ResultInput> results;
}
