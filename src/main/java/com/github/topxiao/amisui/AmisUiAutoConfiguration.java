package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisUiExtension;
import com.github.topxiao.amisui.ext.AmisUiExtensionRegistry;
import com.github.topxiao.amisui.ext.AmisUiPageCustomizer;
import com.github.topxiao.amisui.ext.AmisUiPropertiesCustomizer;
import com.github.topxiao.amisui.ext.AmisUiRenderInterceptor;
import com.github.topxiao.amisui.ext.DefaultAmisUiExtensionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * Amis UI Auto Configuration
 */
@Configuration
@AutoConfiguration
@ConditionalOnClass({AmisUiService.class})
@ConditionalOnProperty(prefix = "amis.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AmisUiProperties.class)
@Import({AmisUiController.class, AmisUiWebConfiguration.class})
public class AmisUiAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AmisUiAutoConfiguration.class);

    /**
     * Amis UI Extension Registry Bean
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public AmisUiExtensionRegistry amisUiExtensionRegistry() {
        return new DefaultAmisUiExtensionRegistry();
    }

    /**
     * 自动注册所有扩展实现
     *
     * @param extensionRegistry     扩展注册表
     * @param extensions            AmisUiExtension 实现列表
     * @param propertiesCustomizers AmisUiPropertiesCustomizer 实现列表
     * @param pageCustomizers       AmisUiPageCustomizer 实现列表
     * @param renderInterceptors    AmisUiRenderInterceptor 实现列表
     */
    @Bean
    @ConditionalOnMissingBean(name = "amisUiExtensionRegistrar")
    public AmisUiExtensionRegistry amisUiExtensionRegistrar(
            AmisUiExtensionRegistry extensionRegistry,
            List<AmisUiExtension> extensions,
            List<AmisUiPropertiesCustomizer> propertiesCustomizers,
            List<AmisUiPageCustomizer> pageCustomizers,
            List<AmisUiRenderInterceptor> renderInterceptors) {

        // 注册 AmisUiExtension 实现
        if (extensions != null) {
            for (AmisUiExtension extension : extensions) {
                extensionRegistry.registerExtension(extension);
                log.debug("Auto-registered extension: {}", extension.getName());
            }
        }

        // 注册 AmisUiPropertiesCustomizer 实现
        if (propertiesCustomizers != null) {
            for (AmisUiPropertiesCustomizer customizer : propertiesCustomizers) {
                // 检查是否已经是 AmisUiExtension（避免重复注册）
                if (customizer instanceof AmisUiExtension) {
                    log.debug("Properties customizer {} is already an AmisUiExtension, skipped",
                            customizer.getClass().getSimpleName());
                } else {
                    extensionRegistry.registerPropertiesCustomizer(customizer);
                    log.debug("Auto-registered properties customizer: {}", customizer.getClass().getSimpleName());
                }
            }
        }

        // 注册 AmisUiPageCustomizer 实现
        if (pageCustomizers != null) {
            for (AmisUiPageCustomizer customizer : pageCustomizers) {
                // 检查是否已经是 AmisUiExtension（避免重复注册）
                if (customizer instanceof AmisUiExtension) {
                    log.debug("Page customizer {} is already an AmisUiExtension, skipped",
                            customizer.getClass().getSimpleName());
                } else {
                    extensionRegistry.registerPageCustomizer(customizer);
                    log.debug("Auto-registered page customizer: {}", customizer.getClass().getSimpleName());
                }
            }
        }

        // 注册 AmisUiRenderInterceptor 实现
        if (renderInterceptors != null) {
            for (AmisUiRenderInterceptor interceptor : renderInterceptors) {
                // 检查是否已经是 AmisUiExtension（避免重复注册）
                if (interceptor instanceof AmisUiExtension) {
                    log.debug("Render interceptor {} is already an AmisUiExtension, skipped",
                            interceptor.getClass().getSimpleName());
                } else {
                    extensionRegistry.registerRenderInterceptor(interceptor);
                    log.debug("Auto-registered render interceptor: {}", interceptor.getClass().getSimpleName());
                }
            }
        }

        return extensionRegistry;
    }

    /**
     * Amis UI Service Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public AmisUiService amisUiService(AmisUiProperties properties,
                                       Environment environment,
                                       ObjectMapper objectMapper,
                                       AmisUiExtensionRegistry extensionRegistry) {
        AmisUiService service = new AmisUiService();
        service.setProperties(properties);
        service.setEnvironment(environment);
        service.setObjectMapper(objectMapper);
        service.setExtensionRegistry(extensionRegistry);
        return service;
    }
}
