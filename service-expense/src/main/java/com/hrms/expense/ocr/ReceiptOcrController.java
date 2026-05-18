package com.hrms.expense.ocr;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/expenses/ocr")
@RequiredArgsConstructor
public class ReceiptOcrController {

    private final ReceiptOcrService ocr;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReceiptOcrService.OcrResult parse(@RequestParam("file") MultipartFile file) {
        return ocr.parseUpload(file);
    }

    @PostMapping("/by-uri")
    public ReceiptOcrService.OcrResult parseStored(@RequestParam String storageUri) {
        return ocr.parseStoredFile(storageUri);
    }
}
