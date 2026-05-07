package com.example.filemanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * WebConfig нь local file serving тохиргоо хийх class юм.
 *
 * Энэ class нь server дээрх local folder дотор хадгалсан file-уудыг
 * /uploads/** URL-аар browser/API client-д харуулах боломжтой болгодог.
 *
 * Анхны local development үед file-ийг server-ийн disk дээр хадгалж test хийхэд хэрэгтэй байсан.
 *
 * Гэхдээ одоогийн Lab 07/08 final architecture дээр file-ууд DigitalOcean Spaces рүү upload хийгдэж байгаа.
 * Тиймээс энэ WebConfig нь үндсэн cloud upload flow-д заавал хэрэгтэй биш.
 *
 * Үүнийг local fallback эсвэл өмнөх local upload туршилтын тохиргоо гэж ойлгож болно.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /*
     * addResourceHandlers method нь static resource mapping нэмдэг.
     *
     * /uploads/** гэсэн URL-ээр орж ирсэн request-ийг
     * server-ийн home directory доторх file-manager-uploads folder руу map хийж байна.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /*
         * Local upload folder-ийн path.
         *
         * Жишээ нь root user дээр ажиллаж байвал:
         * /root/file-manager-uploads/
         *
         * Mac/local дээр ажиллавал:
         * /Users/username/file-manager-uploads/
         */
        String uploadPath = "file:" + System.getProperty("user.home") + "/file-manager-uploads/";

        /*
         * /uploads/** URL pattern-ийг дээрх local folder-той холбож байна.
         *
         * Жишээ:
         * /uploads/image.png
         *
         * гэвэл file-manager-uploads/image.png файлыг serve хийх боломжтой.
         *
         * Одоогийн DigitalOcean Spaces upload flow-д энэ mapping үндсэндээ ашиглагдахгүй,
         * учир нь upload болсон image URL нь Spaces-ийн public URL байдаг.
         */
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}