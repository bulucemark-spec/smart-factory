package com.smartfactory.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfactory.common.result.PageResult;
import com.smartfactory.common.result.Result;
import com.smartfactory.mes.entity.Workstation;
import com.smartfactory.mes.mapper.WorkstationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mes/workstations")
@RequiredArgsConstructor
public class WorkstationController {

    private final WorkstationMapper mapper;

    @GetMapping
    public Result<PageResult<Workstation>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size,
                                                @RequestParam(required = false) String keyword) {
        Page<Workstation> p = new Page<>(page, size);
        LambdaQueryWrapper<Workstation> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Workstation::getCode, keyword).or().like(Workstation::getName, keyword));
        }
        w.orderByAsc(Workstation::getId);
        mapper.selectPage(p, w);
        return Result.ok(PageResult.of(p));
    }

    @GetMapping("/{id}")
    public Result<Workstation> get(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody Workstation entity) {
        mapper.insert(entity);
        return Result.ok(entity.getId());
    }

    @PutMapping
    public Result<Void> update(@RequestBody Workstation entity) {
        mapper.updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
