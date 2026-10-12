package com.smartfactory.qms.controller;

import com.smartfactory.common.result.Result;
import com.smartfactory.qms.entity.Ncr;
import com.smartfactory.qms.service.NcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/qms/ncrs")
@RequiredArgsConstructor
public class NcrController {

    private final NcrService ncrService;

    @GetMapping
    public Result<List<Ncr>> list(@RequestParam(required = false) String status) {
        return Result.ok(ncrService.list(status));
    }

    @GetMapping("/{id}")
    public Result<Ncr> detail(@PathVariable Long id) {
        return Result.ok(ncrService.detail(id));
    }

    @PostMapping("/{id}/mrb")
    public Result<Void> mrb(@PathVariable Long id,
                            @RequestParam String disposition,
                            @RequestParam(required = false) String comment) {
        ncrService.mrb(id, disposition, comment);
        return Result.ok();
    }
}
