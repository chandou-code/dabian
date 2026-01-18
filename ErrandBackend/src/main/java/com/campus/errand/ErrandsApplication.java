package com.campus.errand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 校园跑腿服务系统应用入口
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.campus.errand")
@MapperScan(basePackages = "com.campus.errand.mapper")
public class ErrandsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErrandsApplication.class, args);
    }
}