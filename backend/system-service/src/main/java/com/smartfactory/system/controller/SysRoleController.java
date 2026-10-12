package com.smartfactory.system.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.common.web.BaseCrudController;
import com.smartfactory.system.entity.SysRole;
import com.smartfactory.system.mapper.SysRoleMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/roles")
public class SysRoleController extends BaseCrudController<SysRole> {

    private final SysRoleMapper mapper;

    public SysRoleController(SysRoleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<SysRole> getMapper() {
        return mapper;
    }

    @Override
    protected Wrapper<SysRole> buildQuery(String keyword) {
        LambdaQueryWrapper<SysRole> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(SysRole::getCode, keyword).or().like(SysRole::getName, keyword));
        }
        return w;
    }
}
