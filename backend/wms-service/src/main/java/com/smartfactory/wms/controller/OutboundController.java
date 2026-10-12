package com.smartfactory.wms.controller;

import com.smartfactory.common.context.UserContext;
import com.smartfactory.common.context.UserContextHolder;
import com.smartfactory.common.result.Result;
import com.smartfactory.wms.entity.Outbound;
import com.smartfactory.wms.service.OutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wms/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @GetMapping
    public Result<List<Outbound>> list() {
        return Result.ok(outboundService.list());
    }

    @PostMapping
    public Result<String> create(@RequestBody Outbound outbound) {
        return Result.ok(outboundService.create(outbound));
    }

    @GetMapping("/{id}/detail")
    public Result<Outbound> detail(@PathVariable Long id) {
        return Result.ok(outboundService.detail(id));
    }

    @PostMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        outboundService.ship(id, currentUser());
        return Result.ok();
    }

    private String currentUser() {
        UserContext c = UserContextHolder.get();
        return c != null && c.getUsername() != null ? c.getUsername() : "系统";
    }
}
