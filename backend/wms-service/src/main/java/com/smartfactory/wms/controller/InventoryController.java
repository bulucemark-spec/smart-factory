package com.smartfactory.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfactory.common.result.PageResult;
import com.smartfactory.common.result.Result;
import com.smartfactory.wms.entity.Inventory;
import com.smartfactory.wms.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wms/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryMapper mapper;

    @GetMapping
    public Result<PageResult<Inventory>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size,
                                              @RequestParam(required = false) Long warehouseId,
                                              @RequestParam(required = false) Long materialId,
                                              @RequestParam(required = false) String batchNo,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) String keyword) {
        Page<Inventory> p = new Page<>(page, size);
        LambdaQueryWrapper<Inventory> w = new LambdaQueryWrapper<>();
        if (warehouseId != null) w.eq(Inventory::getWarehouseId, warehouseId);
        if (materialId != null) w.eq(Inventory::getMaterialId, materialId);
        if (StringUtils.hasText(batchNo)) w.eq(Inventory::getBatchNo, batchNo);
        if (StringUtils.hasText(status)) w.eq(Inventory::getStatus, status);
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Inventory::getMaterialCode, keyword)
                    .or().like(Inventory::getMaterialName, keyword));
        }
        w.orderByDesc(Inventory::getId);
        mapper.selectPage(p, w);
        return Result.ok(PageResult.of(p));
    }

    @GetMapping("/summary")
    public Result<List<Map<String, Object>>> summary() {
        return Result.ok(mapper.statusSummary());
    }
}
