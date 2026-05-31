package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisPageCustomizer;
import com.github.topxiao.amisui.ext.AmisPropertiesCustomizer;
import com.github.topxiao.amisui.ext.AmisRenderInterceptor;
import com.github.topxiao.amisui.ext.ClasspathAmisSchemaProvider;
import com.github.topxiao.amisui.ext.PropertiesAppSchemaProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Amis Starter 自动配置。
 * <p>
 * 注册默认 bean，用户可通过 {@code @ConditionalOnMissingBean} 机制替换任意默认实现。
 */
@AutoConfiguration
@ConditionalOnClass(AmisViewService.class)
@ConditionalOnProperty(prefix = "amis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AmisProperties.class)
@Import(AmisWebConfiguration.class)
public class AmisAutoConfiguration {

    /**
     * 核心渲染服务，收集所有扩展点（customizer、interceptor）
     */
    @Bean
    @ConditionalOnMissingBean
    public AmisViewService amisViewService(AmisProperties properties,
                                           Environment environment,
                                           ObjectMapper objectMapper,
                                           @Nullable List<AmisPropertiesCustomizer> propertiesCustomizers,
                                           @Nullable List<AmisPageCustomizer> pageCustomizers,
                                           @Nullable List<AmisRenderInterceptor> renderInterceptors) {
        return new AmisViewService(properties, environment, objectMapper,
                propertiesCustomizers, pageCustomizers, renderInterceptors);
    }

    /**
     * Classpath Schema 解析器（高优先级）。
     * 从 classpath 加载 {name}.json，如果存在 app.json 则覆盖下面的 Properties 实现。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean
    public ClasspathAmisSchemaProvider classpathAmisSchemaProvider(
            AmisProperties properties, ResourceLoader resourceLoader) {
        return new ClasspathAmisSchemaProvider(
                resourceLoader, properties.getSchemaPrefix(), properties.isCacheEnabled());
    }

    /**
     * Properties App Schema 解析器（低优先级，兜底）。
     * 仅处理 "app" 名称，从 AmisProperties 构建 app 配置 JSON。
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    @ConditionalOnMissingBean
    public PropertiesAppSchemaProvider propertiesAppSchemaProvider(
            AmisViewService viewService, ObjectMapper objectMapper) {
        return new PropertiesAppSchemaProvider(viewService, objectMapper);
    }
}
