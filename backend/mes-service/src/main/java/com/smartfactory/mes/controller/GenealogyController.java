package com.smartfactory.mes.controller;

import com.smartfactory.common.result.Result;
import com.smartfactory.mes.entity.Genealogy;
import com.smartfactory.mes.service.GenealogyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mes/genealogy")
@RequiredArgsConstructor
public class GenealogyController {

    private final GenealogyService genealogyService;

    @GetMapping("/backward/{batch}")
    public Result<List<Genealogy>> backward(@PathVariable String batch) {
        return Result.ok(genealogyService.backward(batch));
    }

    @GetMapping("/forward/{batch}")
    public Result<List<Genealogy>> forward(@PathVariable String batch) {
        return Result.ok(genealogyService.forward(batch));
    }
}
