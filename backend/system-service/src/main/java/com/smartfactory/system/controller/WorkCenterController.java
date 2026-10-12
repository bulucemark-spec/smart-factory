package com.smartfactory.system.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.common.web.BaseCrudController;
import com.smartfactory.system.entity.WorkCenter;
import com.smartfactory.system.mapper.WorkCenterMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/work-centers")
public class WorkCenterController extends BaseCrudController<WorkCenter> {

    private final WorkCenterMapper mapper;

    public WorkCenterController(WorkCenterMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<WorkCenter> getMapper() {
        return mapper;
    }

    @Override
    protected Wrapper<WorkCenter> buildQuery(String keyword) {
        LambdaQueryWrapper<WorkCenter> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(WorkCenter::getCode, keyword).or().like(WorkCenter::getName, keyword));
        }
        return w;
    }
}
