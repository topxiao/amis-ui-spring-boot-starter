package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisSchemaProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class AmisViewResolverTest {

    private AmisViewResolver resolver;
    private AmisViewService service;
    private AmisSchemaProvider appProvider;

    @BeforeEach
    void setUp() {
        AmisProperties props = new AmisProperties();
        ObjectMapper mapper = new ObjectMapper();
        var env = new org.springframework.mock.env.MockEnvironment();
        service = new AmisViewService(props, env, mapper, List.of(), List.of(), List.of(), List.of());

        appProvider = new AmisSchemaProvider() {
            @Override
            public String resolveSchema(String name) {
                if (!"app".equals(name)) return null;
                return "{\"type\":\"app\"}";
            }
        };
        resolver = new AmisViewResolver(service, List.of(appProvider));
    }

    @Test
    void resolveViewName_amisApp_returnsAmisViewAppMode() {
        var view = resolver.resolveViewName("amis:app", Locale.getDefault());

        assertThat(view).isInstanceOf(AmisView.class);
        assertThat(((AmisView) view).isAppMode()).isTrue();
    }

    @Test
    void resolveViewName_amisApp_customProvider_usedInTemplate() {
        AmisSchemaProvider customApp = name -> "app".equals(name)
                ? "{\"type\":\"app\",\"brandName\":\"Custom\"}" : null;
        resolver = new AmisViewResolver(service, List.of(customApp));

        var view = resolver.resolveViewName("amis:app", Locale.getDefault());

        assertThat(view).isInstanceOf(AmisView.class);
        String html = ((AmisView) view).renderToString(new HashMap<>());
        assertThat(html).contains("Custom");
    }

    @Test
    void resolveViewName_amisApp_classpathOverridesProperties() {
        AmisSchemaProvider classpathProvider = name -> "app".equals(name)
                ? "{\"type\":\"app\",\"brandName\":\"FromClasspath\"}" : null;
        AmisSchemaProvider propertiesProvider = name -> "app".equals(name)
                ? "{\"type\":\"app\",\"brandName\":\"FromProperties\"}" : null;

        resolver = new AmisViewResolver(service, List.of(classpathProvider, propertiesProvider));
        var view = resolver.resolveViewName("amis:app", Locale.getDefault());

        String html = ((AmisView) view).renderToString(new HashMap<>());
        assertThat(html).contains("FromClasspath");
    }

    @Test
    void resolveViewName_providerReturnsSchema_returnsSchemaModeView() {
        AmisSchemaProvider provider = name -> "users".equals(name) ? "{\"type\":\"page\"}" : null;
        resolver = new AmisViewResolver(service, List.of(appProvider, provider));

        var view = resolver.resolveViewName("amis:users", Locale.getDefault());

        assertThat(view).isInstanceOf(AmisView.class);
        assertThat(((AmisView) view).isAppMode()).isFalse();
    }

    @Test
    void resolveViewName_allProvidersNull_returnsNull() {
        AmisSchemaProvider provider = name -> null;
        resolver = new AmisViewResolver(service, List.of(provider));

        var view = resolver.resolveViewName("amis:unknown", Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void resolveViewName_nonAmis_returnsNull() {
        var view = resolver.resolveViewName("index", Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void resolveViewName_null_returnsNull() {
        var view = resolver.resolveViewName(null, Locale.getDefault());

        assertThat(view).isNull();
    }

    @Test
    void resolveViewName_multipleProviders_firstWins() {
        AmisSchemaProvider first = name -> "page1".equals(name) ? "{\"first\":true}" : null;
        AmisSchemaProvider second = name -> "page1".equals(name) ? "{\"second\":true}" : null;
        resolver = new AmisViewResolver(service, List.of(appProvider, first, second));

        var view = resolver.resolveViewName("amis:page1", Locale.getDefault());

        assertThat(view).isNotNull();
        String html = ((AmisView) view).renderToString(new HashMap<>());
        assertThat(html).contains("\"first\":true");
    }

    @Test
    void resolveViewName_multipleProviders_fallback() {
        AmisSchemaProvider first = name -> null;
        AmisSchemaProvider second = name -> "page2".equals(name) ? "{\"second\":true}" : null;
        resolver = new AmisViewResolver(service, List.of(appProvider, first, second));

        var view = resolver.resolveViewName("amis:page2", Locale.getDefault());

        assertThat(view).isNotNull();
        String html = ((AmisView) view).renderToString(new HashMap<>());
        assertThat(html).contains("\"second\":true");
    }

    @Test
    void resolveViewName_noProviders_returnsNull() {
        resolver = new AmisViewResolver(service, List.of());

        var view = resolver.resolveViewName("amis:users", Locale.getDefault());

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
