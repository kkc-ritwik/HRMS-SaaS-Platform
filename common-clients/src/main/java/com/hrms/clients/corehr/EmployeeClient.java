package com.hrms.clients.corehr;

import com.hrms.clients.corehr.dto.EmployeeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "service-core-hr", path = "/api/v1/employees",
        fallbackFactory = EmployeeClientFallback.class)
public interface EmployeeClient {

    @GetMapping("/{id}")
    EmployeeDto getById(@PathVariable("id") UUID id);

    @GetMapping
    List<EmployeeDto> search(@RequestParam(value = "departmentId", required = false) UUID departmentId,
                              @RequestParam(value = "managerId", required = false) UUID managerId,
                              @RequestParam(value = "status", required = false) String status);

    @GetMapping("/by-user/{userId}")
    EmployeeDto getByUserId(@PathVariable("userId") UUID userId);
}
