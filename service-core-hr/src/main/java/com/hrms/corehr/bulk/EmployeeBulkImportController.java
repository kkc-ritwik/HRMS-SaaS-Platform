package com.hrms.corehr.bulk;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/employees/bulk")
@RequiredArgsConstructor
public class EmployeeBulkImportController {

    private final EmployeeBulkImportService svc;

    @PostMapping(value = "/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmployeeBulkImportService.ImportResult importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return svc.importCsv(file.getInputStream());
    }
}
