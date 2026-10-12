package com.smartfactory.mes.feign;

import com.smartfactory.common.result.Result;
import com.smartfactory.mes.dto.FinishedInboundDto;
import com.smartfactory.mes.dto.PickTask;
import com.smartfactory.mes.dto.PickWaveDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "wms-client", url = "${service.wms.url}")
public interface WmsClient {

    @PostMapping("/internal/pick-waves")
    Result<String> createPickWave(@RequestBody PickWaveDto dto);

    @GetMapping("/internal/pick-waves/by-source/{sourceNo}")
    Result<List<PickTask>> pickResult(@PathVariable("sourceNo") String sourceNo);

    @PostMapping("/internal/finished-goods/inbound")
    Result<Void> finishedInbound(@RequestBody FinishedInboundDto dto);
}
