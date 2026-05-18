package com.hrms.clients.corehr;

import com.hrms.clients.corehr.dto.EmployeeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class EmployeeClientFallback implements FallbackFactory<EmployeeClient> {
    @Override
    public EmployeeClient create(Throwable cause) {
        log.warn("EmployeeClient fallback engaged: {}", cause.getMessage());
        return new EmployeeClient() {
            public EmployeeDto getById(UUID id) { return null; }
            public List<EmployeeDto> search(UUID d, UUID m, String s) { return Collections.emptyList(); }
            public EmployeeDto getByUserId(UUID userId) { return null; }
        };
    }
}
