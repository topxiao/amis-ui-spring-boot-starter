package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisUiExtensionRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Amis UI Service Test
 */
@SpringBootTest
class AmisUiServiceTest {

    @Autowired
    private AmisUiService amisUiService;

    @Autowired
    private AmisUiProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmisUiExtensionRegistry extensionRegistry;

    @Test
    void contextLoads() {
        assertThat(amisUiService).isNotNull();
        assertThat(properties).isNotNull();
        assertThat(objectMapper).isNotNull();
        assertThat(extensionRegistry).isNotNull();
    }

    @Test
    void renderHtml_ShouldReturnValidHtml() {
        String html = amisUiService.renderHtml();

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
}