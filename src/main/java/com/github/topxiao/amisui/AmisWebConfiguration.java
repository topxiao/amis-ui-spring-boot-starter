package com.github.topxiao.amisui;

import com.github.topxiao.amisui.ext.AmisSchemaProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.HttpRequestHandler;

import java.util.List;
import java.util.Map;

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

    @Bean
    public JsonViewResolver jsonViewResolver(@Nullable List<AmisSchemaProvider> providers) {
        return new JsonViewResolver(providers);
    }

    /**
     * 默认 Schema API 端点。
     * <p>
     * 通过 {@link SimpleUrlHandlerMapping} 将 {@code {schemaPath}/**} 注册为处理器，
     * 遍历 {@link AmisSchemaProvider} 链返回 JSON 响应，未匹配返回 404。
     *
     * <pre>
     * GET /schema/users       → classpath:amis/users.json
     * GET /schema/admin/roles → classpath:amis/admin/roles.json
     * </pre>
     */
    @Bean
    @ConditionalOnProperty(prefix = "amis", name = "schema-enabled", havingValue = "true", matchIfMissing = true)
    public SimpleUrlHandlerMapping schemaHandlerMapping(@Nullable List<AmisSchemaProvider> providers) {
        String basePath = properties.getSchemaPath();
        List<AmisSchemaProvider> providerList = providers != null ? providers : List.of();

        HttpRequestHandler handler = (HttpServletRequest request, HttpServletResponse response) -> {
            String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String subPath = path.substring(basePath.length());
            if (subPath.startsWith("/")) {
                subPath = subPath.substring(1);
            }

            for (AmisSchemaProvider provider : providerList) {
                String schema = provider.resolveSchema(subPath);
                if (schema != null) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(schema);
                    return;
                }
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        };

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Map.of(basePath + "/**", handler));
        mapping.setOrder(1);
        return mapping;
    }
}
