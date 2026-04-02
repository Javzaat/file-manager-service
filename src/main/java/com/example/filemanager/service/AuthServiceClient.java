package com.example.filemanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthServiceClient {

    @Value("${soap.service.url}")
    private String soapUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validateToken(String token) {

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        HttpEntity<String> requestEntity = new HttpEntity<>(soapRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                soapUrl,
                requestEntity,
                String.class
        );

        String body = response.getBody();

        System.out.println("SOAP RESPONSE: " + body);

        return body != null &&
                (body.contains("<ns2:valid>true</ns2:valid>") || body.contains("<valid>true</valid>"));
    }
}