//package com.springboot.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.ViewResolver;
//import org.springframework.web.servlet.view.InternalResourceViewResolver;
//
///**
// * Spring Boot Web Configuration สำหรับ JSP + Static Resources
// */
//@Configuration
//public class WebConfig {
//
//    /**
//     * ตั้งค่า ViewResolver สำหรับ JSP
//     * - prefix: โฟลเดอร์ JSP
//     * - suffix: นามสกุลไฟล์ JSP
//     */
//    @Bean
//    public ViewResolver viewResolver() {
//        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//        resolver.setPrefix("/WEB-INF/jsp/"); // วาง JSP ไว้ src/main/webapp/WEB-INF/views/
//        resolver.setSuffix(".jsp");
//        return resolver;
//    }
//
//}

package com.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // =================== CORS Config สำหรับ React ===================
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
