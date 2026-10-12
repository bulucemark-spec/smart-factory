package com.smartfactory.qms.feign;

import com.smartfactory.common.result.Result;
import com.smartfactory.qms.dto.DisposeDto;
import com.smartfactory.qms.dto.InspectionCallbackDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wms-client", url = "${service.wms.url}")
public interface WmsClient {

    @PostMapping("/internal/inventory/inspection-passed")
    Result<Void> inspectionPassed(@RequestBody InspectionCallbackDto dto);

    @PostMapping("/internal/inventory/freeze")
    Result<Void> freeze(@RequestBody InspectionCallbackDto dto);

    @PostMapping("/internal/inventory/release")
    Result<Void> release(@RequestBody InspectionCallbackDto dto);

    @PostMapping("/internal/inventory/dispose")
    Result<Void> dispose(@RequestBody DisposeDto dto);
}
