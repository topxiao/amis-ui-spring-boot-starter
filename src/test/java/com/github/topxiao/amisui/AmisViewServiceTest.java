package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Amis View Service Integration Test
 */
@SpringBootTest
class AmisViewServiceTest {

    @Autowired
    private AmisViewService amisViewService;

    @Autowired
    private AmisProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        assertThat(amisViewService).isNotNull();
        assertThat(properties).isNotNull();
        assertThat(objectMapper).isNotNull();
    }

    @Test
    void renderHtml_ShouldReturnValidHtml() {
        String html = amisViewService.renderHtml();

        assertThat(html).isNotNull();
        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("<title>" + properties.getApp().getTitle() + "</title>");
        assertThat(html).contains("/cdn/amis/" + properties.getVersion() + "/sdk.js");
        assertThat(html).contains("amisRequire('amis/embed')");
    }

    @Test
    void properties_ShouldHaveDefaultValues() {
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getVersion()).isEqualTo("6.12.0");
        assertThat(properties.getApp().getBrandName()).isEqualTo("Admin");
        assertThat(properties.getApp().getTheme()).isEqualTo("ang");
    }

    @Test
    void renderHtml_withSchema_shouldReturnValidHtml() {
        String schemaJson = "{\"type\":\"page\",\"body\":\"Test\"}";
        String html = amisViewService.renderHtml(schemaJson, "Test Page");

        assertThat(html).contains("<!doctype html>");
        assertThat(html).contains("<title>Test Page</title>");
        assertThat(html).contains("Test");
    }
}
