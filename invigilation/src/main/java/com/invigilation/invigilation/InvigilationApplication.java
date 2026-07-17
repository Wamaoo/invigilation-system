//启动整个 Spring Boot 应用
package com.invigilation.invigilation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//标记这是Spring Boot应用，自动配置+组件扫描
@SpringBootApplication
//扫描所有Mapper接口，让MyBatis能找到它们
@MapperScan("com.invigilation.invigilation.mapper")
public class InvigilationApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvigilationApplication.class, args);
    }
}