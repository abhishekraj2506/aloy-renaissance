package com.aloy.coreapp.config;

import com.aloy.coreapp.interceptor.CoreAuthenticationInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableWebMvc
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public CoreAuthenticationInterceptor authenticationInterceptor() {
        return new CoreAuthenticationInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> globalExclusions = new ArrayList<>(List.of("/swagger-ui/index.html**", "/swagger-ui.html**",
                "/v3/api-docs/swagger-config**", "/v3/api-docs**", "/swagger-ui/**",
                "/ondc-buyer/test/user", "/ondc-buyer/test/ondc-response", "/ondc-buyer/test/syncCatalog",
                "/ws/v1/user/globalWs", "/ondc-buyer/test/nft", "/ondc-buyer/test/login", "/ondc-buyer/test/nft/**",
                "/ondc-buyer/api/v1/user/login", "/ondc-buyer/test/badge/**",
                "/ondc-buyer/test/coupon",
                "/healthz**", "/readyz**"));

        registry.addInterceptor(authenticationInterceptor())
                .excludePathPatterns(globalExclusions);
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }
}
