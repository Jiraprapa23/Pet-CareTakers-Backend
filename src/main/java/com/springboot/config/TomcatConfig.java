
package com.springboot.config;

import org.apache.catalina.Context;
import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    // =================== แก้ปัญหา FileCountLimitExceededException ===================
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                // ไม่จำกัดขนาด POST
                connector.setMaxPostSize(-1);
                connector.setMaxParameterCount(10000);
            });
            factory.addContextCustomizers((Context context) -> {
                // ไม่จำกัดจำนวน part ใน multipart
                context.setAllowCasualMultipartParsing(true);
            });
        };
    }
}