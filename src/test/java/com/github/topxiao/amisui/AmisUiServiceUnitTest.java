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
        properties = new AmisUiProperties();
        service = new AmisUiService(properties, null, new ObjectMapper(), null);
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

    // --- null-safe constructor defaults ---

    @Test
    void renderHtml_nullProperties_defaultsToNewProperties() {
        // Constructor defaults null properties to new AmisUiProperties()
        AmisUiService svc = new AmisUiService(null, null, new ObjectMapper(), null);
        assertNotNull(svc.getProperties());
        assertNotNull(svc.getProperties().getApp());
        // Should not throw — rendering works with defaults
        String result = svc.renderHtml();
        assertNotNull(result);
        assertTrue(result.contains("<!DOCTYPE html"));
    }

    @Test
    void renderHtml_schemaJson_nullProperties_defaultsAndRenders() {
        AmisUiService svc = new AmisUiService(null, null, new ObjectMapper(), null);
        String result = svc.renderHtml("{\"type\":\"page\"}");
        assertNotNull(result);
        assertTrue(result.contains("<!doctype html"));
        assertTrue(result.contains("{\"type\":\"page\"}"));
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

        AmisUiService svc = new AmisUiService(properties, null, new ObjectMapper(), registry);

        // Rendering should still succeed despite interceptor exceptions
        String result = svc.renderHtml("{\"type\":\"page\"}");
        assertNotNull(result);
        assertTrue(result.contains("<!doctype html"));
        assertTrue(result.contains("{\"type\":\"page\"}"));
    }
}
