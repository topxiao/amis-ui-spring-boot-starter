package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AmisViewAdaptorTest {

    private AmisViewService createServiceWithAdaptor(String requestAdaptor, String responseAdaptor) {
        AmisProperties props = new AmisProperties();
        props.setRequestAdaptor(requestAdaptor);
        props.setResponseAdaptor(responseAdaptor);
        return new AmisViewService(props, mock(Environment.class),
                new ObjectMapper(), List.of(), List.of(), List.of(), List.of());
    }

    @Test
    void schemaMode_injectsAdaptorScripts() {
        AmisViewService service = createServiceWithAdaptor(
                "api.headers['X-Token'] = 'test'; return api;",
                "return payload.data;"
        );
        AmisView view = new AmisView(service, false);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", "{\"type\":\"page\"}");
        String html = view.renderToString(model);

        assertTrue(html.contains("api.headers['X-Token'] = 'test'; return api;"),
                "Schema mode HTML should contain requestAdaptor script");
        assertTrue(html.contains("return payload.data;"),
                "Schema mode HTML should contain responseAdaptor script");
        assertTrue(html.contains("theme: 'ang'"),
                "Schema mode HTML should contain theme in env options");
    }

    @Test
    void appMode_injectsAdaptorScripts() {
        AmisViewService service = createServiceWithAdaptor(
                "api.headers['Auth'] = token; return api;",
                "if (payload.status === 401) redirect(); return payload;"
        );
        AmisView view = new AmisView(service, true);
        Map<String, Object> model = new HashMap<>();
        String html = view.renderToString(model);

        assertTrue(html.contains("api.headers['Auth'] = token; return api;"),
                "App mode HTML should contain requestAdaptor script");
        assertTrue(html.contains("if (payload.status === 401) redirect(); return payload;"),
                "App mode HTML should contain responseAdaptor script");
    }

    @Test
    void schemaMode_defaultAdaptorWhenNotConfigured() {
        AmisViewService service = createServiceWithAdaptor(null, null);
        AmisView view = new AmisView(service, false);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", "{\"type\":\"page\"}");
        String html = view.renderToString(model);

        assertTrue(html.contains("return api;"),
                "Schema mode should contain default requestAdaptor");
        assertTrue(html.contains("return payload;"),
                "Schema mode should contain default responseAdaptor");
    }
}
