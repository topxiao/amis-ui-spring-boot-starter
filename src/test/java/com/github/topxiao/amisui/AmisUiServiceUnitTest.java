package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisUiExtensionRegistry;
import com.github.topxiao.amisui.ext.DefaultAmisUiExtensionRegistry;
import com.github.topxiao.amisui.ext.AmisUiRenderInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AmisUiServiceUnitTest {

    private AmisUiService service;
    private AmisUiProperties properties;

    @BeforeEach
    void setUp() {
        service = new AmisUiService();
        properties = new AmisUiProperties();
        service.setProperties(properties);
        service.setObjectMapper(new ObjectMapper());
    }

    // --- schemaJson validation ---

    @Test
    void renderHtml_schemaJson_validJson_succeeds() {
        String result = service.renderHtml("{\"type\":\"page\"}");
        assertNotNull(result);
        assertTrue(result.contains("<!doctype html"));
        assertTrue(result.contains("{\"type\":\"page\"}"));
    }

    @Test
    void renderHtml_schemaJson_invalidJson_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.renderHtml("not json at all"));
    }

    @Test
    void renderHtml_schemaJson_xssInjection_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.renderHtml("};alert('xss');//"));
    }

    @Test
    void renderHtml_schemaJson_null_rendersEmptyObject() {
        String result = service.renderHtml((String) null);
        assertNotNull(result);
        assertTrue(result.contains("{}"));
    }

    // --- properties null check ---

    @Test
    void renderHtml_propertiesNull_throwsIllegalState() {
        AmisUiService svc = new AmisUiService();
        svc.setObjectMapper(new ObjectMapper());
        assertThrows(IllegalStateException.class, () -> svc.renderHtml());
    }

    @Test
    void renderHtml_schemaJson_propertiesNull_throwsIllegalState() {
        AmisUiService svc = new AmisUiService();
        svc.setObjectMapper(new ObjectMapper());
        assertThrows(IllegalStateException.class,
            () -> svc.renderHtml("{\"type\":\"page\"}"));
    }

    // --- interceptor exception resilience ---

    @Test
    void renderHtml_interceptorThrowsException_renderingContinues() {
        // Register an interceptor that throws in both beforeRender and afterRender
        AmisUiExtensionRegistry registry = new DefaultAmisUiExtensionRegistry();
        registry.registerRenderInterceptor(new AmisUiRenderInterceptor() {
            @Override
            public void beforeRender(RenderContext context) {
                throw new RuntimeException("Simulated beforeRender failure");
            }

            @Override
            public String afterRender(RenderContext context, String html) {
                throw new RuntimeException("Simulated afterRender failure");
            }
        });
        service.setExtensionRegistry(registry);

        // Rendering should still succeed despite interceptor exceptions
        String result = service.renderHtml("{\"type\":\"page\"}");
        assertNotNull(result);
        assertTrue(result.contains("<!doctype html"));
        assertTrue(result.contains("{\"type\":\"page\"}"));
    }
}
