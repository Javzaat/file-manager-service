package com.example.filemanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/*
 * AuthServiceClient нь File Manager Service-ээс SOAP Authentication Service рүү
 * token validation request илгээх client class юм.
 *
 * File upload хийх үед хэрэглэгч login хийсэн эсэхийг шалгах шаардлагатай.
 * Энэ service token-ийг өөрөө шалгахгүй, харин SOAP service-ийн ValidateToken operation-ийг дууддаг.
 *
 * Ингэснээр authentication logic нэг service дээр буюу User SOAP Service дээр төвлөрч,
 * File Manager Service зөвхөн file upload logic дээр төвлөрнө.
 */
@Service
public class AuthServiceClient {

    /*
     * SOAP Authentication Service-ийн URL.
     *
     * Энэ утга application.properties дээрх soap.service.url property-оос уншигдана.
     * Lab 08 дээр энэ нь ихэвчлэн VPC private IP ашигласан URL байна.
     *
     * Жишээ:
     * http://10.104.0.6:8081/ws
     */
    @Value("${soap.service.url}")
    private String soapUrl;

    /*
     * RestTemplate нь HTTP request илгээхэд ашиглагддаг Spring client.
     *
     * Энд SOAP XML request-ийг SOAP service рүү POST хийхэд ашиглаж байна.
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /*
     * validateToken method нь upload request дээр ирсэн token-ийг SOAP service-ээр шалгуулна.
     *
     * Token valid бол true буцаана.
     * Token invalid бол false буцаана.
     */
    public boolean validateToken(String token) {

        /*
         * SOAP ValidateTokenRequest XML body үүсгэж байна.
         *
         * token утгыг <aut:token> element дотор байрлуулж,
         * SOAP service-ийн ValidateToken operation руу илгээнэ.
         */
        String soapRequest = String.format("""
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:aut="http://example.com/authsoap">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <aut:ValidateTokenRequest>
                         <aut:token>%s</aut:token>
                      </aut:ValidateTokenRequest>
                   </soapenv:Body>
                </soapenv:Envelope>
                """, token);

        /*
         * SOAP request-ийн HTTP header үүсгэж байна.
         * SOAP message нь XML format-той тул Content-Type нь text/xml байна.
         */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        /*
         * XML body болон header-ийг нэг request entity болгож байна.
         * RestTemplate энэ entity-г SOAP service рүү илгээнэ.
         */
        HttpEntity<String> requestEntity = new HttpEntity<>(soapRequest, headers);

        /*
         * SOAP service рүү POST request илгээж байна.
         *
         * soapUrl нь SOAP service-ийн /ws endpoint.
         * response body нь XML string хэлбэрээр ирнэ.
         */
        ResponseEntity<String> response = restTemplate.postForEntity(
                soapUrl,
                requestEntity,
                String.class
        );

        /*
         * SOAP response body-г авна.
         */
        String body = response.getBody();

        /*
         * Demo/debug үед SOAP service-ээс яг ямар response ирж байгааг log дээр харахын тулд хэвлэж байна.
         */
        System.out.println("SOAP RESPONSE: " + body);

        /*
         * SOAP response дотор valid=true байгаа эсэхийг шалгаж байна.
         *
         * Зарим response namespace-тэй <ns2:valid>true</ns2:valid> гэж ирж болно.
         * Зарим үед namespace prefix-гүй <valid>true</valid> гэж ирж болно.
         * Тиймээс хоёр хувилбарыг хоёуланг нь шалгаж байна.
         *
         * Хэрэв valid=true байвал file upload үргэлжилнэ.
         * Үгүй бол FileService дээр Invalid token error өгнө.
         */
        return body != null &&
                (body.contains("<ns2:valid>true</ns2:valid>") || body.contains("<valid>true</valid>"));
    }
}