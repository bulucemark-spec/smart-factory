package com.smartfactory.system.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.common.web.BaseCrudController;
import com.smartfactory.system.entity.SysOrg;
import com.smartfactory.system.mapper.SysOrgMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/orgs")
public class SysOrgController extends BaseCrudController<SysOrg> {

    private final SysOrgMapper mapper;

    public SysOrgController(SysOrgMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<SysOrg> getMapper() {
        return mapper;
    }

    @Override
    protected Wrapper<SysOrg> buildQuery(String keyword) {
        LambdaQueryWrapper<SysOrg> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(SysOrg::getName, keyword);
        }
        w.orderByAsc(SysOrg::getSort);
        return w;
    }
}
