package com.smartfactory.common.web;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfactory.common.entity.BaseEntity;
import com.smartfactory.common.result.PageResult;
import com.smartfactory.common.result.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 通用 CRUD 控制器基类：分页/详情/新增/修改/删除
 * 子类只需提供 Mapper，并可重写 buildQuery 实现关键字检索
 */
public abstract class BaseCrudController<T extends BaseEntity> {

    protected abstract BaseMapper<T> getMapper();

    protected Wrapper<T> buildQuery(String keyword) {
        return new LambdaQueryWrapper<>();
    }

    @GetMapping
    public Result<PageResult<T>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "20") long size,
                                      @RequestParam(required = false) String keyword) {
        Page<T> p = new Page<>(page, size);
        getMapper().selectPage(p, buildQuery(keyword));
        return Result.ok(PageResult.of(p));
    }

    @GetMapping("/{id}")
    public Result<T> get(@PathVariable Long id) {
        return Result.ok(getMapper().selectById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody T entity) {
        getMapper().insert(entity);
        return Result.ok(entity.getId());
    }

    @PutMapping
    public Result<Void> update(@RequestBody T entity) {
        getMapper().updateById(entity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        getMapper().deleteById(id);
        return Result.ok();
    }
}
