package com.smartfactory.mes.feign;

import com.smartfactory.common.result.Result;
import com.smartfactory.mes.dto.InspectionTaskCreateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "qms-client", url = "${service.qms.url}")
public interface QmsClient {

    @PostMapping("/internal/inspection/task")
    Result<String> createInspectionTask(@RequestBody InspectionTaskCreateDto dto);
}
