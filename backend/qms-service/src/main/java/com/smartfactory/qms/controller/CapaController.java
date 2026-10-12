package com.smartfactory.qms.controller;

import com.smartfactory.common.context.UserContext;
import com.smartfactory.common.context.UserContextHolder;
import com.smartfactory.common.result.Result;
import com.smartfactory.qms.entity.Capa;
import com.smartfactory.qms.service.CapaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/qms/capas")
@RequiredArgsConstructor
public class CapaController {

    private final CapaService capaService;

    @GetMapping
    public Result<List<Capa>> list(@RequestParam(required = false) String status) {
        return Result.ok(capaService.list(status));
    }

    @GetMapping("/{id}")
    public Result<Capa> detail(@PathVariable Long id) {
        return Result.ok(capaService.detail(id));
    }

    @PostMapping("/execute")
    public Result<Void> execute(@RequestBody Capa capa) {
        capaService.execute(capa);
        return Result.ok();
    }

    @PostMapping("/{id}/verify")
    public Result<Void> verify(@PathVariable Long id,
                               @RequestParam(required = false) String verifyResult,
                               @RequestParam Boolean passed) {
        UserContext c = UserContextHolder.get();
        capaService.verify(id, verifyResult, passed, c != null ? c.getUsername() : "质量部");
        return Result.ok();
    }
}
