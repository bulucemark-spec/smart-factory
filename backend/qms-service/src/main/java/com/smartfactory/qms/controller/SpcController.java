package com.smartfactory.qms.controller;

import com.smartfactory.common.result.Result;
import com.smartfactory.qms.entity.SpcRecord;
import com.smartfactory.qms.service.SpcService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/qms/spc")
@RequiredArgsConstructor
public class SpcController {

    private final SpcService spcService;

    @PostMapping("/record")
    public Result<Void> record(@RequestBody SpcRecord record) {
        spcService.add(record);
        return Result.ok();
    }

    @GetMapping("/chart")
    public Result<Map<String, Object>> chart(@RequestParam String characteristic) {
        return Result.ok(spcService.chart(characteristic));
    }

    @GetMapping("/cpk")
    public Result<Map<String, Object>> cpk(@RequestParam String characteristic,
                                           @RequestParam double lsl,
                                           @RequestParam double usl) {
        return Result.ok(spcService.cpk(characteristic, lsl, usl));
    }
}
