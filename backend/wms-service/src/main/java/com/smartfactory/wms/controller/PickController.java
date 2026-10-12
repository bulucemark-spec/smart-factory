package com.smartfactory.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.context.UserContext;
import com.smartfactory.common.context.UserContextHolder;
import com.smartfactory.common.result.Result;
import com.smartfactory.wms.entity.PickTask;
import com.smartfactory.wms.entity.PickWave;
import com.smartfactory.wms.mapper.PickTaskMapper;
import com.smartfactory.wms.mapper.PickWaveMapper;
import com.smartfactory.wms.service.PickService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wms/pick")
@RequiredArgsConstructor
public class PickController {

    private final PickService pickService;
    private final PickWaveMapper waveMapper;
    private final PickTaskMapper taskMapper;

    @GetMapping("/waves")
    public Result<List<PickWave>> waves() {
        return Result.ok(waveMapper.selectList(
                new LambdaQueryWrapper<PickWave>().orderByDesc(PickWave::getId)));
    }

    @GetMapping("/waves/{id}/tasks")
    public Result<List<PickTask>> tasks(@PathVariable Long id) {
        return Result.ok(taskMapper.selectList(
                new LambdaQueryWrapper<PickTask>().eq(PickTask::getWaveId, id)));
    }

    @PostMapping("/waves/{id}/execute")
    public Result<Void> execute(@PathVariable Long id) {
        pickService.executeWave(id, currentUser());
        return Result.ok();
    }

    @GetMapping("/by-source/{sourceNo}")
    public Result<List<PickTask>> bySource(@PathVariable String sourceNo) {
        return Result.ok(pickService.resultBySourceNo(sourceNo));
    }

    private String currentUser() {
        UserContext c = UserContextHolder.get();
        return c != null && c.getUsername() != null ? c.getUsername() : "系统";
    }
}
