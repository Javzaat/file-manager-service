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

/*
 * FileService нь File Manager Service-ийн үндсэн business logic class юм.
 *
 * Энэ class-ийн гол үүрэг:
 * - Upload request дээр ирсэн Authorization token-ийг шалгах
 * - Token-ийг SOAP Auth Service-ээр validate хийлгэх
 * - File хоосон эсэхийг шалгах
 * - DigitalOcean Spaces object storage рүү file upload хийх
 * - Upload болсон file-ийн public URL-ийг буцаах
 *
 * DigitalOcean Spaces нь AWS S3 compatible API ашигладаг тул
 * AmazonS3 client ашиглаж upload хийж байна.
 */
@Service
public class FileService {

    /*
     * AuthServiceClient нь SOAP Authentication Service-тэй харилцана.
     *
     * File upload хийх request бүр дээр token valid эсэхийг шалгах хэрэгтэй.
     * Энэ service өөрөө token шалгахгүй, SOAP service-ийн ValidateToken operation руу дамжуулна.
     */
    private final AuthServiceClient authServiceClient;

    /*
     * AmazonS3 client нь DigitalOcean Spaces рүү file upload хийхэд ашиглагдана.
     *
     * DigitalOcean Spaces нь S3 compatible тул AWS SDK ашиглаж болно.
     */
    private final AmazonS3 s3Client;

    /*
     * Upload хийх DigitalOcean Spaces bucket-ийн нэр.
     *
     * Жишээ:
     * javzaa-images
     */
    private final String bucketName;

    /*
     * Constructor injection.
     *
     * Spring Boot application.properties дээрээс s3 endpoint, access key, secret key,
     * bucket name зэрэг тохиргоог уншиж авна.
     *
     * API key болон secret key-г code дотор шууд бичихгүй,
     * environment variable/application.properties ашиглаж байгаа нь security талдаа зөв.
     */
    public FileService(AuthServiceClient authServiceClient,
                       @Value("${s3.endpoint}") String endpoint,
                       @Value("${s3.access.key}") String accessKey,
                       @Value("${s3.secret.key}") String secretKey,
                       @Value("${s3.bucket}") String bucketName) {

        /*
         * Token validate хийх client-ийг хадгалж байна.
         */
        this.authServiceClient = authServiceClient;

        /*
         * Bucket нэрийг class field-д хадгалж байна.
         */
        this.bucketName = bucketName;

        /*
         * DigitalOcean Spaces-д холбогдох access key болон secret key-ээр credential үүсгэж байна.
         *
         * DigitalOcean Spaces AWS S3 API-тай compatible тул BasicAWSCredentials ашиглаж болно.
         */
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        /*
         * AmazonS3 client үүсгэж байна.
         *
         * endpoint нь DigitalOcean Spaces endpoint.
         * Жишээ:
         * https://sgp1.digitaloceanspaces.com
         *
         * region дээр "us-east-1" гэж өгсөн байгаа.
         * DigitalOcean Spaces дээр AWS SDK ашиглах үед region заавал хэрэгтэй байдаг,
         * гэхдээ endpoint нь DigitalOcean-ийн endpoint тул upload DigitalOcean Spaces рүү очно.
         */
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-1"))

                /*
                 * Path style access disabled.
                 *
                 * Энэ нь bucket endpoint format-ийг DigitalOcean Spaces-ийн public URL хэлбэртэй
                 * ажиллуулахад хэрэглэгдэж байна.
                 */
                .withPathStyleAccessEnabled(false)

                /*
                 * S3 client-д access credential өгч байна.
                 */
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    /*
     * uploadFile method нь зураг/file upload хийх үндсэн method юм.
     *
     * Controller-оос MultipartFile болон Authorization header ирнэ.
     * Method дараах алхмаар ажиллана:
     * 1. Authorization header шалгах
     * 2. Bearer token салгаж авах
     * 3. SOAP service-ээр token validate хийх
     * 4. File хоосон эсэхийг шалгах
     * 5. Unique file name үүсгэх
     * 6. DigitalOcean Spaces рүү upload хийх
     * 7. Public file URL буцаах
     */
    public String uploadFile(MultipartFile file, String authHeader) {

        /*
         * Authorization header байхгүй эсвэл Bearer format биш бол upload зөвшөөрөхгүй.
         *
         * File upload нь protected operation тул зөвхөн login хийсэн хэрэглэгч upload хийх ёстой.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Unauthorized");
        }

        /*
         * "Bearer " гэдэг эхний 7 тэмдэгтийг хасаад зөвхөн token хэсгийг авч байна.
         */
        String token = authHeader.substring(7);

        /*
         * Token-ийг SOAP Authentication Service-ээр validate хийлгэж байна.
         *
         * AuthServiceClient нь ValidateToken SOAP request илгээж true/false response авна.
         */
        boolean valid = authServiceClient.validateToken(token);

        /*
         * Token invalid бол upload хийхгүй.
         */
        if (!valid) {
            throw new RuntimeException("Invalid token");
        }

        /*
         * File хоосон эсвэл null байвал upload хийх боломжгүй.
         */
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            /*
             * File name давхардахгүй байлгахын тулд одоогийн timestamp-ийг
             * original file name-ийн өмнө залгаж байна.
             *
             * Жишээ:
             * 1778123505207_chart_icon.png
             */
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            /*
             * DigitalOcean Spaces рүү upload хийх request үүсгэж байна.
             *
             * bucketName  -> ямар bucket рүү upload хийх
             * fileName    -> bucket дотор хадгалагдах object key
             * inputStream -> upload хийх file-ийн content
             */
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)

                            /*
                             * PublicRead болгож байгаа тул upload болсон зураг public URL-аар харагдана.
                             * Энэ URL-ийг profile imageUrl талбарт хадгалж frontend дээр харуулах боломжтой.
                             */
                            .withCannedAcl(CannedAccessControlList.PublicRead);

            /*
             * File-ийг DigitalOcean Spaces bucket рүү upload хийж байна.
             */
            s3Client.putObject(putObjectRequest);

            /*
             * Upload болсон file-ийн public URL-ийг авч frontend рүү буцаана.
             *
             * Дараа нь frontend энэ URL-ийг profile create/update request дээр imageUrl талбар болгон
             * User JSON Service рүү хадгалуулдаг.
             */
            return s3Client.getUrl(bucketName, fileName).toString();

        } catch (Exception e) {
            /*
             * Upload явцад алдаа гарвал log дээр stack trace хэвлээд,
             * frontend/API Gateway рүү ойлгомжтой error message буцаана.
             */
            e.printStackTrace();
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}