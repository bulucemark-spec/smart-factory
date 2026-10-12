package com.smartfactory.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.common.context.UserContext;
import com.smartfactory.common.context.UserContextHolder;
import com.smartfactory.common.result.Result;
import com.smartfactory.wms.entity.Location;
import com.smartfactory.wms.entity.PutawayTask;
import com.smartfactory.wms.mapper.PutawayTaskMapper;
import com.smartfactory.wms.service.PutawayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wms/putaway")
@RequiredArgsConstructor
public class PutawayController {

    private final PutawayService putawayService;
    private final PutawayTaskMapper taskMapper;

    @GetMapping("/tasks")
    public Result<List<PutawayTask>> tasks(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<PutawayTask> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(PutawayTask::getStatus, status);
        w.orderByDesc(PutawayTask::getId);
        return Result.ok(taskMapper.selectList(w));
    }

    @GetMapping("/recommend")
    public Result<List<Location>> recommend(@RequestParam Long warehouseId) {
        return Result.ok(putawayService.recommend(warehouseId));
    }

    @PostMapping("/{id}/execute")
    public Result<Void> execute(@PathVariable Long id, @RequestParam Long toLocationId) {
        putawayService.execute(id, toLocationId, currentUser());
        return Result.ok();
    }

    private String currentUser() {
        UserContext c = UserContextHolder.get();
        return c != null && c.getUsername() != null ? c.getUsername() : "系统";
    }
}
