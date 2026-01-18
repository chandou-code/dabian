package com.campus.errand.common.config;

import com.campus.errand.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT认证拦截器
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/check-username/**",
                        "/auth/check-phone/**",
                        "/test/**",
                        "/public/**",
                        "/uploads/**",
                        "/upload/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置跨域
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        System.out.println("CORS配置已加载");
    }
}
