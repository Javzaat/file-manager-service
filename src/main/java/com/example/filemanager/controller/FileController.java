package com.example.filemanager.controller;

import com.example.filemanager.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader
    ) {
        return fileService.uploadFile(file, authHeader);
    }
}