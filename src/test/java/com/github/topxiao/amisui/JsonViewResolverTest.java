package com.github.topxiao.amisui;

import com.github.topxiao.amisui.ext.AmisSchemaProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

class JsonViewResolverTest {

    private JsonViewResolver resolver;
    private AmisSchemaProvider provider;

    @BeforeEach
    void setUp() {
        provider = name -> "users".equals(name) ? "{\"type\":\"page\"}" : null;
        resolver = new JsonViewResolver(List.of(provider));
    }

    @Test
    void resolveViewName_jsonPrefix_withMatch_returnsJsonView() {
        var view = resolver.resolveViewName("json:users", Locale.getDefault());

        assertThat(view).isInstanceOf(JsonView.class);
    }

    @Test
    void resolveViewName_jsonPrefix_withMatch_contentIsFromProvider() throws Exception {
        var view = resolver.resolveViewName("json:users", Locale.getDefault());
        var response = new org.springframework.mock.web.MockHttpServletResponse();

        view.render(new java.util.HashMap<>(), new org.springframework.mock.web.MockHttpServletRequest(), response);

        assertThat(response.getContentAsString()).isEqualTo("{\"type\":\"page\"}");
    }

    @Test
    void resolveViewName_jsonPrefix_noMatch_returnsNull() {
        AmisSchemaProvider emptyProvider = name -> null;
        resolver = new JsonViewResolver(List.of(emptyProvider));

        var view = resolver.resolveViewName("json:unknown", Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void resolveViewName_nonJsonPrefix_returnsNull() {
        var view = resolver.resolveViewName("amis:users", Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void resolveViewName_null_returnsNull() {
        var view = resolver.resolveViewName(null, Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void resolveViewName_emptyString_returnsNull() {
        var view = resolver.resolveViewName("", Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void resolveViewName_multipleProviders_firstWins() throws Exception {
        AmisSchemaProvider first = name -> "page1".equals(name) ? "{\"first\":true}" : null;
        AmisSchemaProvider second = name -> "page1".equals(name) ? "{\"second\":true}" : null;
        resolver = new JsonViewResolver(List.of(first, second));

        var view = resolver.resolveViewName("json:page1", Locale.getDefault());

        assertThat(view).isNotNull();
        assertThat(view).isInstanceOf(JsonView.class);
        var response = new org.springframework.mock.web.MockHttpServletResponse();
        try {
            view.render(new java.util.HashMap<>(), new org.springframework.mock.web.MockHttpServletRequest(), response);
        } catch (Exception e) {
            fail("render threw exception", e);
        }
        assertThat(response.getContentAsString()).isEqualTo("{\"first\":true}");
    }

    @Test
    void resolveViewName_noProviders_returnsNull() {
        resolver = new JsonViewResolver(List.of());

        var view = resolver.resolveViewName("json:users", Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void resolveViewName_nullProviders_returnsNull() {
        resolver = new JsonViewResolver(null);

        var view = resolver.resolveViewName("json:users", Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void getOrder_defaultIsHighestPrecedence() {
        assertThat(resolver.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    @Test
    void setOrder_changesOrder() {
        resolver.setOrder(10);
        assertThat(resolver.getOrder()).isEqualTo(10);
    }
}
