package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisRenderContext;
import com.github.topxiao.amisui.ext.AmisRenderInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmisViewServiceUnitTest {

    private AmisViewService service;
    private AmisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AmisProperties();
        service = new AmisViewService(properties, null, new ObjectMapper(), List.of(), List.of(), List.of());
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
        // Constructor defaults null properties to new AmisProperties()
        AmisViewService svc = new AmisViewService(null, null, new ObjectMapper(), List.of(), List.of(), List.of());
        assertNotNull(svc.getProperties());
        assertNotNull(svc.getProperties().getApp());
        // Should not throw — rendering works with defaults
        String result = svc.renderHtml();
        assertNotNull(result);
        assertTrue(result.contains("<!DOCTYPE html"));
    }

    @Test
    void renderHtml_schemaJson_nullProperties_defaultsAndRenders() {
        AmisViewService svc = new AmisViewService(null, null, new ObjectMapper(), List.of(), List.of(), List.of());
        String result = svc.renderHtml("{\"type\":\"page\"}");
        assertNotNull(result);
        assertTrue(result.contains("<!doctype html"));
        assertTrue(result.contains("{\"type\":\"page\"}"));
    }

    // --- interceptor exception resilience ---

    @Test
    void renderHtml_interceptorThrowsException_renderingContinues() {
        // Pass an interceptor that throws in both beforeRender and afterRender
        AmisRenderInterceptor throwingInterceptor = new AmisRenderInterceptor() {
            @Override
            public void beforeRender(AmisRenderContext context) {
                throw new RuntimeException("Simulated beforeRender failure");
            }

            @Override
            public String afterRender(AmisRenderContext context, String html) {
                throw new RuntimeException("Simulated afterRender failure");
            }
        };

        AmisViewService svc = new AmisViewService(properties, null, new ObjectMapper(),
                List.of(), List.of(), List.of(throwingInterceptor));

        // Rendering should still succeed despite interceptor exceptions
        String result = svc.renderHtml("{\"type\":\"page\"}");
        assertNotNull(result);
        assertTrue(result.contains("<!doctype html"));
        assertTrue(result.contains("{\"type\":\"page\"}"));
    }
}
