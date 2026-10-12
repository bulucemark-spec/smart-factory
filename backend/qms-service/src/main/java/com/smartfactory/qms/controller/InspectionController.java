package com.smartfactory.qms.controller;

import com.smartfactory.common.context.UserContextHolder;
import com.smartfactory.common.result.Result;
import com.smartfactory.qms.dto.SubmitResultDto;
import com.smartfactory.qms.entity.InspectionTask;
import com.smartfactory.qms.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qms/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @GetMapping
    public Result<List<InspectionTask>> list(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) String type) {
        return Result.ok(inspectionService.list(status, type));
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(inspectionService.taskDetail(id));
    }

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody SubmitResultDto dto) {
        inspectionService.submit(dto, UserContextHolder.get());
        return Result.ok();
    }
}
