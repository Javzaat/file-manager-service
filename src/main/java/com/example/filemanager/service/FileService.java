package com.example.filemanager.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    private final AuthServiceClient authServiceClient;

    public FileService(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public String uploadFile(MultipartFile file, String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }

        String token = authHeader.substring(7);

        boolean valid = authServiceClient.validateToken(token);

        if (!valid) {
            throw new RuntimeException("Invalid token");
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            Path uploadPath = Paths.get(System.getProperty("user.home"), "file-manager-uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.write(filePath, file.getBytes());

            return "http://localhost:8082/uploads/" + fileName;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }
}