package sit.int204.mobileshop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        registry.addResourceHandler("/sale-items-images/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(String resourcePath,
                                                                               org.springframework.core.io.Resource location) throws IOException {
                        org.springframework.core.io.Resource requested = location.createRelative(resourcePath);
                        try {
                            if (requested.exists() && requested.isReadable()) {
                                return requested;
                            }
                        } catch (Exception ignored) {}
                        return location.createRelative("default.jpg");
                    }
                });
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://ip24kk1.sit.kmutt.ac.th",
                        "http://intproj24.sit.kmutt.ac.th"
                )

                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

}
