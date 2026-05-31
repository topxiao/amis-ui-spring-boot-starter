package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisPageCustomizer;
import com.github.topxiao.amisui.ext.AmisPropertiesCustomizer;
import com.github.topxiao.amisui.ext.AmisRenderInterceptor;
import com.github.topxiao.amisui.ext.AmisSchemaProvider;
import com.github.topxiao.amisui.ext.ClasspathAmisSchemaProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(AmisViewService.class)
@ConditionalOnProperty(prefix = "amis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AmisProperties.class)
@Import(AmisWebConfiguration.class)
public class AmisAutoConfiguration {

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

    @Bean
    @ConditionalOnMissingBean(AmisSchemaProvider.class)
    public ClasspathAmisSchemaProvider classpathAmisSchemaProvider(
            AmisProperties properties, ResourceLoader resourceLoader) {
        return new ClasspathAmisSchemaProvider(
                resourceLoader, properties.getSchemaPrefix(), properties.isCacheEnabled());
    }
}
