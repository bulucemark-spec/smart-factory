package com.smartfactory.wms.controller;

import com.smartfactory.common.result.Result;
import com.smartfactory.wms.dto.DisposeDto;
import com.smartfactory.wms.dto.FinishedInboundDto;
import com.smartfactory.wms.dto.InspectionCallbackDto;
import com.smartfactory.wms.dto.PickWaveDto;
import com.smartfactory.wms.entity.PickTask;
import com.smartfactory.wms.service.PickService;
import com.smartfactory.wms.service.PutawayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 服务间内部接口（仅容器网络可达，不经 gateway 暴露）
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final PutawayService putawayService;
    private final PickService pickService;

    @PostMapping("/inventory/inspection-passed")
    public Result<Void> passed(@RequestBody InspectionCallbackDto dto) {
        putawayService.onPassed(dto);
        return Result.ok();
    }

    @PostMapping("/inventory/freeze")
    public Result<Void> freeze(@RequestBody InspectionCallbackDto dto) {
        putawayService.onFreeze(dto);
        return Result.ok();
    }

    @PostMapping("/inventory/release")
    public Result<Void> release(@RequestBody InspectionCallbackDto dto) {
        putawayService.onRelease(dto);
        return Result.ok();
    }

    @PostMapping("/inventory/dispose")
    public Result<Void> dispose(@RequestBody DisposeDto dto) {
        putawayService.dispose(dto);
        return Result.ok();
    }

    @PostMapping("/pick-waves")
    public Result<String> createPickWave(@RequestBody PickWaveDto dto) {
        return Result.ok(pickService.createWave(dto));
    }

    @GetMapping("/pick-waves/by-source/{sourceNo}")
    public Result<List<PickTask>> pickResult(@PathVariable String sourceNo) {
        return Result.ok(pickService.resultBySourceNo(sourceNo));
    }

    @PostMapping("/finished-goods/inbound")
    public Result<Void> finishedInbound(@RequestBody FinishedInboundDto dto) {
        pickService.finishedInbound(dto);
        return Result.ok();
    }
}
