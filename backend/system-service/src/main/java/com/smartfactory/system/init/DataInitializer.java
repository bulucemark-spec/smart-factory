package com.smartfactory.system.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartfactory.system.entity.SysUser;
import com.smartfactory.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动初始化演示账号（BCrypt 加密），幂等
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        create("admin", "admin123", "系统管理员", 2L, 1L);
        create("planner", "planner123", "计划员", 2L, 2L);
        create("warehouse", "warehouse123", "仓管员", 2L, 3L);
        create("inspector", "inspector123", "质量检验员", 2L, 4L);
        create("operator", "operator123", "一线操作工", 2L, 5L);
    }

    private void create(String username, String rawPassword, String realName, Long orgId, Long roleId) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (count != null && count > 0) {
            return;
        }
        SysUser user = new SysUser();
        user.setOrgId(orgId);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRealName(realName);
        user.setStatus(1);
        userMapper.insert(user);
        userMapper.insertUserRole(user.getId(), roleId);
        log.info("初始化演示账号: {}/{}", username, rawPassword);
    }
}
