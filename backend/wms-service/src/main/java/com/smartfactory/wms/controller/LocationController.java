package com.smartfactory.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfactory.common.result.PageResult;
import com.smartfactory.common.result.Result;
import com.smartfactory.wms.entity.Location;
import com.smartfactory.wms.mapper.LocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wms/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationMapper mapper;

    @GetMapping
    public Result<PageResult<Location>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size,
                                             @RequestParam(required = false) Long warehouseId,
                                             @RequestParam(required = false) String keyword) {
        Page<Location> p = new Page<>(page, size);
        LambdaQueryWrapper<Location> w = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            w.eq(Location::getWarehouseId, warehouseId);
        }
        if (StringUtils.hasText(keyword)) {
            w.like(Location::getCode, keyword);
        }
        w.orderByAsc(Location::getId);
        mapper.selectPage(p, w);
        return Result.ok(PageResult.of(p));
    }

    @GetMapping("/{id}")
    public Result<Location> get(@PathVariable Long id) {
        return Result.ok(mapper.selectById(id));
    }
}
