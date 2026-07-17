//配置跨域、安全、数据库等，前端能访问后端
package com.invigilation.invigilation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
// 配置类
@Configuration
public class CorsConfig {
// 定义CorsFilter Bean，解决前端跨域请求问题
    @Bean
    public CorsFilter corsFilter() {
        //创建跨域配置对象
        CorsConfiguration config = new CorsConfiguration();
        //允许携带Cookie凭证、所有HTTP方法、请求头
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        //配置URL匹配规则
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        //返回跨域过滤器
        return new CorsFilter(source);
    }
}