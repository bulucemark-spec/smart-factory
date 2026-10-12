package com.smartfactory.system.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.common.web.BaseCrudController;
import com.smartfactory.system.entity.Material;
import com.smartfactory.system.mapper.MaterialMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/materials")
public class MaterialController extends BaseCrudController<Material> {

    private final MaterialMapper mapper;

    public MaterialController(MaterialMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<Material> getMapper() {
        return mapper;
    }

    @Override
    protected Wrapper<Material> buildQuery(String keyword) {
        LambdaQueryWrapper<Material> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Material::getCode, keyword).or().like(Material::getName, keyword));
        }
        return w;
    }
}
