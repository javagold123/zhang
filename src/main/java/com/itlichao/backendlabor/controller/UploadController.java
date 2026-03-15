package com.itlichao.backendlabor.controller;

import com.itlichao.backendlabor.common.Result;
import com.itlichao.backendlabor.service.MinioStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final MinioStorageService storageService;

    public UploadController(MinioStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(@RequestPart("file") MultipartFile file,
                                                   @RequestParam(value = "category", required = false) String category) {
        String url = storageService.uploadImage(file, category);
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        return Result.ok(data);
    }
}
