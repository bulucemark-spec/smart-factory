package com.smartfactory.system.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfactory.common.result.Result;
import com.smartfactory.common.web.BaseCrudController;
import com.smartfactory.system.entity.Bom;
import com.smartfactory.system.mapper.BomMapper;
import com.smartfactory.system.service.BomService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/boms")
public class BomController extends BaseCrudController<Bom> {

    private final BomMapper mapper;
    private final BomService bomService;

    public BomController(BomMapper mapper, BomService bomService) {
        this.mapper = mapper;
        this.bomService = bomService;
    }

    @Override
    protected BaseMapper<Bom> getMapper() {
        return mapper;
    }

    @Override
    protected Wrapper<Bom> buildQuery(String keyword) {
        LambdaQueryWrapper<Bom> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(Bom::getCode, keyword);
        }
        w.orderByDesc(Bom::getId);
        return w;
    }

    @GetMapping("/{id}/detail")
    public Result<Bom> detail(@PathVariable Long id) {
        return Result.ok(bomService.detail(id));
    }

    @PostMapping("/save")
    public Result<Long> save(@RequestBody Bom bom) {
        return Result.ok(bomService.saveWithItems(bom));
    }
}
