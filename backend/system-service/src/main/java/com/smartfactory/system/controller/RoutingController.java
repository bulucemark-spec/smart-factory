package com.smartfactory.system.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.common.result.Result;
import com.smartfactory.common.web.BaseCrudController;
import com.smartfactory.system.entity.Routing;
import com.smartfactory.system.mapper.RoutingMapper;
import com.smartfactory.system.service.RoutingService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/routings")
public class RoutingController extends BaseCrudController<Routing> {

    private final RoutingMapper mapper;
    private final RoutingService routingService;

    public RoutingController(RoutingMapper mapper, RoutingService routingService) {
        this.mapper = mapper;
        this.routingService = routingService;
    }

    @Override
    protected BaseMapper<Routing> getMapper() {
        return mapper;
    }

    @Override
    protected Wrapper<Routing> buildQuery(String keyword) {
        LambdaQueryWrapper<Routing> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(Routing::getCode, keyword);
        }
        w.orderByDesc(Routing::getId);
        return w;
    }

    @GetMapping("/{id}/detail")
    public Result<Routing> detail(@PathVariable Long id) {
        return Result.ok(routingService.detail(id));
    }

    @PostMapping("/save")
    public Result<Long> save(@RequestBody Routing routing) {
        return Result.ok(routingService.saveWithOperations(routing));
    }
}
