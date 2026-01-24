package com.example.demo.config;

import com.example.demo.interceptor.HttpLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcInterceptorConfig implements WebMvcConfigurer {

    private final HttpLoggingInterceptor httpLoggingInterceptor;

    public WebMvcInterceptorConfig(HttpLoggingInterceptor httpLoggingInterceptor) {
        this.httpLoggingInterceptor = httpLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/error",
                    "/actuator/**",
                    "/static/**",
                    "/public/**",
                    "/resources/**",
                    "/META-INF/resources/**"
                );
    }
}