package com.example.filemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/*
 * CorsConfig нь File Manager Service-ийн CORS тохиргоог хийдэг class юм.
 *
 * CORS нь browser өөр domain-оос backend service рүү request илгээх үед
 * зөвшөөрөх эсэхийг шийддэг security mechanism юм.
 *
 * Lab 08 architecture дээр frontend ихэвчлэн шууд File Manager Service рүү хандахгүй,
 * API Gateway-ээр дамждаг.
 *
 * Гэхдээ энэ тохиргоог үлдээсэн шалтгаан:
 * - local development үед frontend-ээс шууд test хийх боломжтой байх
 * - DigitalOcean frontend domain-оос шууд request test хийх боломжтой байх
 * - OPTIONS preflight request-ийг зөв handle хийх
 */
@Configuration
public class CorsConfig {

    /*
     * CorsFilter bean нь request бүр дээр CORS header нэмэх үүрэгтэй.
     *
     * @Order(Ordered.HIGHEST_PRECEDENCE) ашигласнаар энэ filter бусад filter-үүдээс өмнө ажиллана.
     * Ингэснээр browser-ийн OPTIONS preflight request зөв буцаж чадна.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {

        /*
         * CORS тохиргооны object үүсгэж байна.
         */
        CorsConfiguration config = new CorsConfiguration();

        /*
         * Request илгээхийг зөвшөөрөх origin-ууд.
         *
         * 127.0.0.1:5500 нь local frontend test хийх үед ашиглагдана.
         * frontend-app-a4t6q.ondigitalocean.app нь DigitalOcean дээр deploy хийсэн frontend.
         */
        config.setAllowedOriginPatterns(List.of(
                "http://127.0.0.1:5500",
                "https://frontend-app-a4t6q.ondigitalocean.app"
        ));

        /*
         * Зөвшөөрөх HTTP method-ууд.
         *
         * File upload голчлон POST ашигладаг.
         * OPTIONS нь browser preflight request-д хэрэгтэй.
         */
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        /*
         * Бүх header-ийг зөвшөөрч байна.
         *
         * Authorization header-аар token дамжих тул энэ тохиргоо хэрэгтэй.
         */
        config.setAllowedHeaders(List.of("*"));

        /*
         * Cookie/session credential ашиглахгүй.
         *
         * Манай authentication token нь Authorization header-аар дамжиж байгаа.
         */
        config.setAllowCredentials(false);

        /*
         * Энэ CORS тохиргоог бүх endpoint дээр хэрэглэнэ.
         */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        /*
         * CORS filter-ийг Spring Boot application context руу буцааж байна.
         */
        return new CorsFilter(source);
    }
}