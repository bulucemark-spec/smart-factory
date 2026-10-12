package com.smartfactory.qms.controller;

import com.smartfactory.common.result.Result;
import com.smartfactory.qms.dto.InspectionTaskCreateDto;
import com.smartfactory.qms.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final InspectionService inspectionService;

    @PostMapping("/inspection/task")
    public Result<String> createInspectionTask(@RequestBody InspectionTaskCreateDto dto) {
        return Result.ok(inspectionService.createTask(dto));
    }
}
