package com.smartfactory.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfactory.common.exception.BusinessException;
import com.smartfactory.common.exception.ErrorCode;
import com.smartfactory.common.result.PageResult;
import com.smartfactory.common.result.Result;
import com.smartfactory.system.entity.SysUser;
import com.smartfactory.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping
    public Result<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size,
                                            @RequestParam(required = false) String keyword) {
        Page<SysUser> p = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        }
        userMapper.selectPage(p, w);
        return Result.ok(PageResult.of(p));
    }

    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        return Result.ok(userMapper.selectById(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody SysUser user) {
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return Result.ok(user.getId());
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // null 不参与更新，保留原密码
        }
        userMapper.updateById(user);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.ok();
    }

    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String password) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(u);
        return Result.ok();
    }

    @GetMapping("/{id}/roles")
    public Result<List<String>> roles(@PathVariable Long id) {
        return Result.ok(userMapper.findRoleCodes(id));
    }

    @PostMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userMapper.deleteUserRoles(id);
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                userMapper.insertUserRole(id, roleId);
            }
        }
        return Result.ok();
    }
}
