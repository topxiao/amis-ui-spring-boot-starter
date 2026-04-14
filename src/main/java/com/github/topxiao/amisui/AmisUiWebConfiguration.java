package com.github.topxiao.amisui;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Amis UI Web Configuration
 */
@Configuration
@ConditionalOnProperty(prefix = "amis.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AmisUiWebConfiguration implements WebMvcConfigurer {

    /**
     * Configure static resource handlers for Amis SDK files
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /cdn/amis/ requests to the Amis SDK resources in classpath
        registry.addResourceHandler("/cdn/amis/**")
                .addResourceLocations("classpath:/static/cdn/amis/")
                .setCachePeriod(3600); // Cache for 1 hour
    }
}