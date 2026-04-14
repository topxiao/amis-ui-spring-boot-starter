package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisUiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Amis UI 扩展点注册表默认实现
 *
 * @author zyarn
 * @version 1.0.0
 */
public class DefaultAmisUiExtensionRegistry implements AmisUiExtensionRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultAmisUiExtensionRegistry.class);

    private final List<AmisUiExtension> extensions = new CopyOnWriteArrayList<>();
    private final List<AmisUiPropertiesCustomizer> propertiesCustomizers = new CopyOnWriteArrayList<>();
    private final List<AmisUiPageCustomizer> pageCustomizers = new CopyOnWriteArrayList<>();
    private final List<AmisUiRenderInterceptor> renderInterceptors = new CopyOnWriteArrayList<>();

    @Override
    public void registerExtension(AmisUiExtension extension) {
        if (extension == null) {
            log.warn("Cannot register null extension");
            return;
        }
        extensions.add(extension);
        
        // 如果同时是自定义器，也添加到对应的列表
        if (extension instanceof AmisUiPropertiesCustomizer) {
            registerPropertiesCustomizer((AmisUiPropertiesCustomizer) extension);
        }
        if (extension instanceof AmisUiPageCustomizer) {
            registerPageCustomizer((AmisUiPageCustomizer) extension);
        }
        if (extension instanceof AmisUiRenderInterceptor) {
            registerRenderInterceptor((AmisUiRenderInterceptor) extension);
        }
        
        log.debug("Registered extension: {}", extension.getName());
    }

    @Override
    public void registerPropertiesCustomizer(AmisUiPropertiesCustomizer customizer) {
        if (customizer == null) {
            log.warn("Cannot register null properties customizer");
            return;
        }
        // 避免重复添加
        if (!propertiesCustomizers.contains(customizer)) {
            propertiesCustomizers.add(customizer);
            log.debug("Registered properties customizer: {}", customizer.getClass().getSimpleName());
        }
    }

    @Override
    public void registerPageCustomizer(AmisUiPageCustomizer customizer) {
        if (customizer == null) {
            log.warn("Cannot register null page customizer");
            return;
        }
        // 避免重复添加
        if (!pageCustomizers.contains(customizer)) {
            pageCustomizers.add(customizer);
            log.debug("Registered page customizer: {}", customizer.getClass().getSimpleName());
        }
    }

    @Override
    public void registerRenderInterceptor(AmisUiRenderInterceptor interceptor) {
        if (interceptor == null) {
            log.warn("Cannot register null render interceptor");
            return;
        }
        // 避免重复添加
        if (!renderInterceptors.contains(interceptor)) {
            renderInterceptors.add(interceptor);
            log.debug("Registered render interceptor: {}", interceptor.getClass().getSimpleName());
        }
    }

    @Override
    public List<AmisUiPropertiesCustomizer> getPropertiesCustomizers() {
        return propertiesCustomizers.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public List<AmisUiRenderInterceptor> getRenderInterceptors() {
        return renderInterceptors.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public List<AmisUiPageCustomizer> getPageCustomizers() {
        return pageCustomizers.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
    }

    @Override
    public AmisUiProperties applyPropertiesCustomizers(AmisUiProperties properties) {
        if (properties == null) {
            return null;
        }
        AmisUiProperties result = properties;
        for (AmisUiPropertiesCustomizer customizer : getPropertiesCustomizers()) {
            try {
                AmisUiProperties customized = customizer.customize(result);
                if (customized != null) {
                    result = customized;
                    log.debug("Applied properties customizer: {}", customizer.getClass().getSimpleName());
                }
            } catch (Exception e) {
                log.error("Failed to apply properties customizer: {}", customizer.getClass().getSimpleName(), e);
            }
        }
        return result;
    }
}
