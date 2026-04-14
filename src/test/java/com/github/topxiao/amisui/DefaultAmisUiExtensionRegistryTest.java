package com.github.topxiao.amisui;

import com.github.topxiao.amisui.ext.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultAmisUiExtensionRegistryTest {

    private DefaultAmisUiExtensionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultAmisUiExtensionRegistry();
    }

    @Test
    void registerPropertiesCustomizer_addsToList() {
        AmisUiPropertiesCustomizer customizer = props -> props;
        registry.registerPropertiesCustomizer(customizer);
        assertEquals(1, registry.getPropertiesCustomizers().size());
    }

    @Test
    void registerPropertiesCustomizer_preventsDuplicate() {
        AmisUiPropertiesCustomizer customizer = props -> props;
        registry.registerPropertiesCustomizer(customizer);
        registry.registerPropertiesCustomizer(customizer);
        assertEquals(1, registry.getPropertiesCustomizers().size());
    }

    @Test
    void registerPropertiesCustomizer_null_ignored() {
        registry.registerPropertiesCustomizer(null);
        assertEquals(0, registry.getPropertiesCustomizers().size());
    }

    @Test
    void registerRenderInterceptor_addsToList() {
        AmisUiRenderInterceptor interceptor = new AmisUiRenderInterceptor() {
            @Override
            public void beforeRender(RenderContext context) {}
            @Override
            public String afterRender(RenderContext context, String html) { return html; }
        };
        registry.registerRenderInterceptor(interceptor);
        assertEquals(1, registry.getRenderInterceptors().size());
    }

    @Test
    void registerPageCustomizer_addsToList() {
        AmisUiPageCustomizer customizer = (pages, props) -> pages;
        registry.registerPageCustomizer(customizer);
        assertEquals(1, registry.getPageCustomizers().size());
    }

    @Test
    void applyPropertiesCustomizers_appliesInOrder() {
        registry.registerPropertiesCustomizer(props -> {
            props.getApp().setTitle("First");
            return props;
        });
        registry.registerPropertiesCustomizer(props -> {
            props.getApp().setTitle(props.getApp().getTitle() + "+Second");
            return props;
        });

        AmisUiProperties props = new AmisUiProperties();
        AmisUiProperties result = registry.applyPropertiesCustomizers(props);
        assertEquals("First+Second", result.getApp().getTitle());
    }

    @Test
    void applyPropertiesCustomizers_nullInput_returnsNull() {
        assertNull(registry.applyPropertiesCustomizers(null));
    }

    @Test
    void registerExtension_plainExtension_doesNotRegisterAsCustomizer() {
        AmisUiExtension extension = new AmisUiExtension() {
            @Override public String getName() { return "TestExt"; }
        };
        registry.registerExtension(extension);
        // 纯 AmisUiExtension 不实现子接口，customizers 应为空
        assertEquals(0, registry.getPropertiesCustomizers().size());
        assertEquals(0, registry.getPageCustomizers().size());
        assertEquals(0, registry.getRenderInterceptors().size());
    }
}
