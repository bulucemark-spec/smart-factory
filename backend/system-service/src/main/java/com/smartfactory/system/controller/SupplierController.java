package com.smartfactory.system.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.common.web.BaseCrudController;
import com.smartfactory.system.entity.Supplier;
import com.smartfactory.system.mapper.SupplierMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/suppliers")
public class SupplierController extends BaseCrudController<Supplier> {

    private final SupplierMapper mapper;

    public SupplierController(SupplierMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<Supplier> getMapper() {
        return mapper;
    }

    @Override
    protected Wrapper<Supplier> buildQuery(String keyword) {
        LambdaQueryWrapper<Supplier> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Supplier::getCode, keyword).or().like(Supplier::getName, keyword));
        }
        return w;
    }
}
