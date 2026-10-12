package com.smartfactory.mes.controller;

import com.smartfactory.common.context.UserContextHolder;
import com.smartfactory.common.result.Result;
import com.smartfactory.mes.dto.PickWaveDto;
import com.smartfactory.mes.dto.ReportDto;
import com.smartfactory.mes.entity.WorkOrder;
import com.smartfactory.mes.entity.WorkReport;
import com.smartfactory.mes.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mes/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public Result<List<WorkOrder>> list(@RequestParam(required = false) String status) {
        return Result.ok(workOrderService.list(status));
    }

    @GetMapping("/{id}")
    public Result<WorkOrder> detail(@PathVariable Long id) {
        return Result.ok(workOrderService.detail(id));
    }

    @PostMapping
    public Result<WorkOrder> create(@RequestBody WorkOrder wo) {
        return Result.ok(workOrderService.create(wo));
    }

    @PostMapping("/{id}/release")
    public Result<Void> release(@PathVariable Long id,
                                @RequestBody(required = false) PickWaveDto wave) {
        workOrderService.release(id, wave);
        return Result.ok();
    }

    @PostMapping("/{id}/receive-materials")
    public Result<Void> receive(@PathVariable Long id) {
        workOrderService.receiveMaterials(id);
        return Result.ok();
    }

    @PostMapping("/{id}/start")
    public Result<Void> start(@PathVariable Long id) {
        workOrderService.start(id);
        return Result.ok();
    }

    @PostMapping("/report")
    public Result<Void> report(@RequestBody ReportDto dto) {
        workOrderService.report(dto, UserContextHolder.get());
        return Result.ok();
    }

    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        workOrderService.complete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        workOrderService.close(id);
        return Result.ok();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        workOrderService.cancel(id);
        return Result.ok();
    }

    @GetMapping("/{id}/reports")
    public Result<List<WorkReport>> reports(@PathVariable Long id) {
        return Result.ok(workOrderService.reports(id));
    }
}
