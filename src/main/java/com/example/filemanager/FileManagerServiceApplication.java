package com.example.filemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * FileManagerServiceApplication нь File Manager Service-ийн main class юм.
 *
 * Spring Boot application энэ class-аас эхэлж ажиллана.
 *
 * Энэ service-ийн үндсэн үүрэг:
 * - /files/upload endpoint-оор image/file upload request хүлээж авах
 * - Authorization token-ийг SOAP Authentication Service-ээр validate хийх
 * - DigitalOcean Spaces bucket рүү file upload хийх
 * - Upload болсон file-ийн public URL-ийг frontend рүү буцаах
 *
 * Lab 07 дээр энэ service нь distributed file management буюу
 * object storage ашиглах хэсгийг хэрэгжүүлж байгаа.
 *
 * Lab 08 дээр энэ service нь API Gateway-ийн ард private service хэлбэрээр байрлаж,
 * frontend шууд энэ service рүү биш, Gateway-ээр дамжиж ханддаг.
 */
@SpringBootApplication
public class FileManagerServiceApplication {

    /*
     * main method нь File Manager Service Spring Boot application-ийг эхлүүлнэ.
     *
     * Application асах үед:
     * - Spring context үүснэ
     * - FileController бүртгэгдэнэ
     * - FileService болон AuthServiceClient bean-үүд үүснэ
     * - application.properties дээрх S3 болон SOAP тохиргоонууд уншигдана
     * - Tomcat server 8082 port дээр ажиллаж эхэлнэ
     */
    public static void main(String[] args) {
        SpringApplication.run(FileManagerServiceApplication.class, args);
    }

}