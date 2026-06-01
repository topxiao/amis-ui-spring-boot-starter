package com.github.topxiao.amisui;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;

class JsonViewTest {

    @Test
    void getContentType_returnsApplicationJson() {
        JsonView view = new JsonView("{\"type\":\"page\"}");

        assertThat(view.getContentType()).isEqualTo("application/json;charset=UTF-8");
    }

    @Test
    void render_writesJsonToResponse() throws Exception {
        String json = "{\"type\":\"page\",\"title\":\"Users\"}";
        JsonView view = new JsonView(json);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        view.render(new HashMap<>(), request, response);

        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(response.getContentAsString()).isEqualTo(json);
    }

    @Test
    void render_nonJsonContent_passedThrough() throws Exception {
        String content = "this is not json";
        JsonView view = new JsonView(content);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        view.render(new HashMap<>(), request, response);

        assertThat(response.getContentAsString()).isEqualTo(content);
    }

    @Test
    void render_nullModel_writesJsonToResponse() throws Exception {
        String json = "{\"type\":\"page\"}";
        JsonView view = new JsonView(json);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        view.render(null, request, response);

        assertThat(response.getContentAsString()).isEqualTo(json);
    }
}
