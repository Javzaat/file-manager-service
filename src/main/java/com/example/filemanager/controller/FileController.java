package com.example.filemanager.controller;

import com.example.filemanager.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
 * FileController нь File Manager Service-ийн REST controller юм.
 *
 * Энэ controller-ийн гол үүрэг:
 * - Frontend/API Gateway-ээс ирсэн image upload request-ийг хүлээж авах
 * - Authorization header-ийг хамт авах
 * - FileService рүү file болон token-ийг дамжуулах
 *
 * Lab 07 дээр File Manager Service нэмэх шаардлагатай байсан.
 * Энэ service нь хэрэглэгчийн profile зураг upload хийж,
 * DigitalOcean Spaces object storage дээр хадгалдаг.
 */
@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class FileController {

    /*
     * FileService нь upload-ийн үндсэн logic-ийг хариуцна.
     *
     * Controller нь зөвхөн request авч service рүү дамжуулах үүрэгтэй.
     * Харин FileService нь:
     * - token validate хийх
     * - file name үүсгэх
     * - DigitalOcean Spaces рүү upload хийх
     * - image URL буцаах
     *
     * зэрэг logic-ийг гүйцэтгэнэ.
     */
    private final FileService fileService;

    /*
     * Constructor injection.
     *
     * Spring Boot FileService bean-ийг автоматаар inject хийж өгнө.
     */
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /*
     * POST /files/upload
     *
     * Энэ endpoint нь multipart/form-data request хүлээж авна.
     * Frontend дээр хэрэглэгч зураг сонгоод upload хийх үед энэ endpoint дуудагдана.
     *
     * API Gateway дээр:
     * /api/files/upload
     *
     * гэж ирсэн request нь File Manager Service-ийн:
     * /files/upload
     *
     * endpoint рүү proxy хийгддэг.
     */
    @PostMapping("/upload")
    public String uploadFile(
            /*
             * "file" нэртэй form field-ээс upload хийсэн файлыг авна.
             *
             * MultipartFile нь Spring-ийн upload file represent хийдэг class.
             * Үүгээр file name, content type, size, byte stream зэрэг мэдээллийг авч болно.
             */
            @RequestParam("file") MultipartFile file,

            /*
             * Authorization header-ийг request-ээс авч байна.
             *
             * Энэ header дотор:
             * Bearer <token>
             *
             * хэлбэрээр login token ирнэ.
             * FileService энэ token-ийг SOAP service-ээр validate хийлгэж,
             * зөв token байвал upload-ийг үргэлжлүүлнэ.
             */
            @RequestHeader("Authorization") String authHeader
    ) {
        /*
         * Upload-ийн үндсэн logic-ийг FileService рүү шилжүүлж байна.
         * Амжилттай upload болсон тохиолдолд DigitalOcean Spaces дээрх image URL буцна.
         */
        return fileService.uploadFile(file, authHeader);
    }
}