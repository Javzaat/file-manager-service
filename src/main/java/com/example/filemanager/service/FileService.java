package com.example.filemanager.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final AuthServiceClient authServiceClient;
    private final AmazonS3 s3Client;
    private final String bucketName;

    public FileService(AuthServiceClient authServiceClient,
                       @Value("${s3.endpoint}") String endpoint,
                       @Value("${s3.access.key}") String accessKey,
                       @Value("${s3.secret.key}") String secretKey,
                       @Value("${s3.bucket}") String bucketName) {

        this.authServiceClient = authServiceClient;
        this.bucketName = bucketName;

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-1"))
                .withPathStyleAccessEnabled(false)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
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
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)
                            .withCannedAcl(CannedAccessControlList.PublicRead);

            s3Client.putObject(putObjectRequest);

            return s3Client.getUrl(bucketName, fileName).toString();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}