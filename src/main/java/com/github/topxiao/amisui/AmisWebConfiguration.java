package com.github.topxiao.amisui;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(prefix = "amis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AmisWebConfiguration implements WebMvcConfigurer {

    private final AmisProperties properties;

    public AmisWebConfiguration(AmisProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/cdn/amis/**")
                .addResourceLocations("classpath:/static/cdn/amis/")
                .setCachePeriod(3600);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(properties.getPath()).setViewName("amis:app");
    }

    @Bean
    public AmisViewResolver amisViewResolver(AmisViewService viewService) {
        return new AmisViewResolver(viewService);
    }
}
