package com.smartfactory.wms.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.common.web.BaseCrudController;
import com.smartfactory.wms.entity.Warehouse;
import com.smartfactory.wms.mapper.WarehouseMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wms/warehouses")
public class WarehouseController extends BaseCrudController<Warehouse> {

    private final WarehouseMapper mapper;

    public WarehouseController(WarehouseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<Warehouse> getMapper() {
        return mapper;
    }

    @Override
    protected Wrapper<Warehouse> buildQuery(String keyword) {
        LambdaQueryWrapper<Warehouse> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Warehouse::getCode, keyword).or().like(Warehouse::getName, keyword));
        }
        return w;
    }
}
