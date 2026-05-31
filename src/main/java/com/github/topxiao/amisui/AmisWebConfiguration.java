package com.github.topxiao.amisui;

import com.github.topxiao.amisui.ext.AmisSchemaProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.List;

import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Amis Web MVC 配置。
 * <p>
 * 注册 CDN 静态资源映射、默认路由（/amis → amis:app 视图）和 {@link AmisViewResolver} Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "amis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AmisWebConfiguration implements WebMvcConfigurer {

    private final AmisProperties properties;

    public AmisWebConfiguration(AmisProperties properties) {
        this.properties = properties;
    }

    /**
     * 将 /cdn/amis/** 映射到 classpath:/static/cdn/amis/，缓存 1 小时
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/cdn/amis/**")
                .addResourceLocations("classpath:/static/cdn/amis/")
                .setCachePeriod(3600);
    }

    /**
     * 注册默认 ViewController：访问 amis.path 路径时自动返回 amis:app 视图
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(properties.getPath()).setViewName("amis:app");
    }

    /**
     * 注册 ViewResolver，注入所有 AmisSchemaProvider 实现
     */
    @Bean
    public AmisViewResolver amisViewResolver(AmisViewService viewService,
                                             @Nullable List<AmisSchemaProvider> providers) {
        return new AmisViewResolver(viewService, providers);
    }
}
