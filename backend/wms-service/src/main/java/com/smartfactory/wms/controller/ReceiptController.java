package com.smartfactory.wms.controller;

import com.smartfactory.common.result.Result;
import com.smartfactory.wms.entity.Receipt;
import com.smartfactory.wms.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wms/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping
    public Result<List<Receipt>> list() {
        return Result.ok(receiptService.list());
    }

    @PostMapping("/receive")
    public Result<String> receive(@RequestBody Receipt receipt) {
        return Result.ok(receiptService.receive(receipt));
    }

    @GetMapping("/{id}/detail")
    public Result<Receipt> detail(@PathVariable Long id) {
        return Result.ok(receiptService.detail(id));
    }
}
