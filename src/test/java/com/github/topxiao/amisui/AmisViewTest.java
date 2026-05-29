package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisUiExtensionRegistry;
import com.github.topxiao.amisui.ext.AmisUiRenderInterceptor;
import com.github.topxiao.amisui.ext.AmisUiRenderInterceptor.RenderContext;
import com.github.topxiao.amisui.ext.DefaultAmisUiExtensionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class AmisViewTest {

    private AmisUiService service;
    private AmisUiProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AmisUiProperties();
        service = new AmisUiService(properties, null, new ObjectMapper(), null);
    }

    // -------------------------------------------------------------------------
    // Schema mode tests
    // -------------------------------------------------------------------------

    @Test
    void schemaMode_withJsonString_rendersHtml() {
        AmisView view = new AmisView(service, false);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", "{\"type\":\"page\",\"title\":\"Test\"}");

        String html = view.renderToString(model);

        assertThat(html).contains("<!doctype html");
        assertThat(html).contains("<title>AMIS Page</title>");
        assertThat(html).contains("\"type\":\"page\"");
        assertThat(html).contains("schemaJson");
    }

    @Test
    void schemaMode_withMap_rendersHtml() {
        AmisView view = new AmisView(service, false);
        Map<String, Object> model = new HashMap<>();
        Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put("type", "page");
        schemaMap.put("title", "Map Test");
        model.put("schema", schemaMap);

        String html = view.renderToString(model);

        assertThat(html).contains("<!doctype html");
        assertThat(html).contains("\"type\":\"page\"");
        assertThat(html).contains("\"title\":\"Map Test\"");
    }

    @Test
    void schemaMode_missingSchema_throwsException() {
        AmisView view = new AmisView(service, false);
        Map<String, Object> model = new HashMap<>();

        assertThatThrownBy(() -> view.renderToString(model))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("schema attribute is required for amis:page view");
    }

    @Test
    void schemaMode_customTitle_overrideDefault() {
        AmisView view = new AmisView(service, false);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", "{\"type\":\"page\"}");
        model.put("title", "Custom Title");

        String html = view.renderToString(model);

        assertThat(html).contains("<title>Custom Title</title>");
        assertThat(html).doesNotContain("<title>AMIS Page</title>");
    }

    @Test
    void schemaMode_customCssAndJs_injected() {
        AmisView view = new AmisView(service, false);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", "{\"type\":\"page\"}");
        model.put("customCss", "body { background: red; }");
        model.put("customJs", "console.log('hello')");

        String html = view.renderToString(model);

        assertThat(html).contains("body { background: red; }");
        assertThat(html).contains("console.log('hello')");
    }

    @Test
    void getContentType_returnsTextHtml() {
        AmisView view = new AmisView(service, false);

        assertThat(view.getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    // -------------------------------------------------------------------------
    // App mode tests
    // -------------------------------------------------------------------------

    @Test
    void appMode_rendersHtmlWithAppConfig() {
        AmisView view = new AmisView(service, true);
        Map<String, Object> model = new HashMap<>();

        String html = view.renderToString(model);

        assertThat(html).contains("<!DOCTYPE html");
        assertThat(html).contains("\"type\":\"app\"");
        assertThat(html).contains("\"brandName\":\"Admin\"");
    }

    @Test
    void appMode_noPages_rendersWithoutException() {
        // properties.pages is null by default
        AmisView view = new AmisView(service, true);
        Map<String, Object> model = new HashMap<>();

        String html = view.renderToString(model);

        assertThat(html).contains("<!DOCTYPE html");
        assertThat(html).contains("\"type\":\"app\"");
        // No pages key in app config when pages list is null/empty
        assertThat(html).doesNotContain("\"pages\"");
    }

    // -------------------------------------------------------------------------
    // Interceptor tests
    // -------------------------------------------------------------------------

    @Test
    void schemaMode_renderInterceptor_applied() {
        // Register an after-render interceptor that modifies the output
        AmisUiExtensionRegistry registry = new DefaultAmisUiExtensionRegistry();
        registry.registerRenderInterceptor(new AmisUiRenderInterceptor() {
            @Override
            public String afterRender(RenderContext context, String html) {
                return html.replace("</html>", "<!-- intercepted --></html>");
            }
        });
        // Recreate service with the registry
        AmisUiService svcWithRegistry = new AmisUiService(properties, null, new ObjectMapper(), registry);

        AmisView view = new AmisView(svcWithRegistry, false);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", "{\"type\":\"page\"}");

        String html = view.renderToString(model);

        assertThat(html).contains("<!-- intercepted -->");
    }

    // -------------------------------------------------------------------------
    // renderToString tests
    // -------------------------------------------------------------------------

    @Test
    void renderToString_returnsHtmlString() throws UnsupportedEncodingException {
        AmisView view = new AmisView(service, false);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", "{\"type\":\"page\"}");

        String html = view.renderToString(model);

        assertThat(html).isNotBlank();
        assertThat(html).startsWith("<!doctype html");
        assertThat(html).contains("</html>");

        // Also verify that render() produces the same output via response
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        model.put("schema", "{\"type\":\"page\"}");

        assertThatCode(() -> view.render(model, request, response)).doesNotThrowAnyException();
        assertThat(response.getContentType()).isEqualTo("text/html;charset=UTF-8");
        String responseContent = response.getContentAsString();
        assertThat(responseContent).contains("<!doctype html");
    }
}
