package com.example.demo.config;

import com.example.demo.interceptor.AuthenticationInterceptor;
import com.example.demo.interceptor.HttpLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcInterceptorConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final HttpLoggingInterceptor httpLoggingInterceptor;

    public WebMvcInterceptorConfig(
            AuthenticationInterceptor authenticationInterceptor,
            HttpLoggingInterceptor httpLoggingInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.httpLoggingInterceptor = httpLoggingInterceptor;
    }

    /**
     * 配置 CORS 跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加认证拦截器,优先级最高(order=0)
        registry.addInterceptor(authenticationInterceptor)
                .order(0)
                .addPathPatterns("/**")
                .excludePathPatterns("/error");

        // 添加日志拦截器,优先级次之(order=1)
        registry.addInterceptor(httpLoggingInterceptor)
                .order(1)
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