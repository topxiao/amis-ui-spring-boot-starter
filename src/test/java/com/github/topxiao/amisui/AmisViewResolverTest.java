package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class AmisViewResolverTest {

    private AmisViewResolver resolver;

    @BeforeEach
    void setUp() {
        AmisProperties props = new AmisProperties();
        ObjectMapper mapper = new ObjectMapper();
        var env = new org.springframework.mock.env.MockEnvironment();
        AmisViewService service = new AmisViewService(props, env, mapper, List.of(), List.of(), List.of());
        resolver = new AmisViewResolver(service);
    }

    @Test
    void resolveViewName_amisPage_returnsAmisViewSchemaMode() {
        var view = resolver.resolveViewName("amis:page", Locale.getDefault());

        assertThat(view).isInstanceOf(AmisView.class);
        assertThat(((AmisView) view).isAppMode()).isFalse();
    }

    @Test
    void resolveViewName_amisApp_returnsAmisViewAppMode() {
        var view = resolver.resolveViewName("amis:app", Locale.getDefault());

        assertThat(view).isInstanceOf(AmisView.class);
        assertThat(((AmisView) view).isAppMode()).isTrue();
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
    void resolveViewName_amisUnknown_returnsNull() {
        var view = resolver.resolveViewName("amis:unknown", Locale.getDefault());

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
