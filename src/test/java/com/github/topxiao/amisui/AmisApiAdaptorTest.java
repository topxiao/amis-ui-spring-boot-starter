package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisApiAdaptor;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AmisApiAdaptorTest {

    private AmisViewService createService(List<AmisApiAdaptor> adaptors) {
        return createService(null, null, adaptors);
    }

    private AmisViewService createService(String requestAdaptor, String responseAdaptor,
                                           List<AmisApiAdaptor> adaptors) {
        AmisProperties props = new AmisProperties();
        if (requestAdaptor != null) props.setRequestAdaptor(requestAdaptor);
        if (responseAdaptor != null) props.setResponseAdaptor(responseAdaptor);
        return new AmisViewService(props, mock(Environment.class),
                new ObjectMapper(), List.of(), List.of(), List.of(), adaptors);
    }

    @Test
    void resolveRequestAdaptor_defaultValue() {
        AmisViewService service = createService(List.of());
        assertEquals("return api;", service.resolveRequestAdaptor());
    }

    @Test
    void resolveResponseAdaptor_defaultValue() {
        AmisViewService service = createService(List.of());
        assertEquals("return payload;", service.resolveResponseAdaptor());
    }

    @Test
    void resolveRequestAdaptor_fromProperties() {
        AmisViewService service = createService("api.headers['X'] = '1'; return api;", null, List.of());
        assertEquals("api.headers['X'] = '1'; return api;", service.resolveRequestAdaptor());
    }

    @Test
    void resolveResponseAdaptor_fromProperties() {
        AmisViewService service = createService(null, "return payload.data;", List.of());
        assertEquals("return payload.data;", service.resolveResponseAdaptor());
    }

    @Test
    void resolveRequestAdaptor_beanOverridesProperties() {
        AmisApiAdaptor bean = new AmisApiAdaptor() {
            @Override
            public String getRequestAdaptor() {
                return "api.headers['Auth'] = token; return api;";
            }
        };
        AmisViewService service = createService("from-properties", null, List.of(bean));
        assertEquals("api.headers['Auth'] = token; return api;", service.resolveRequestAdaptor());
    }

    @Test
    void resolveRequestAdaptor_beanNull_fallsToProperties() {
        AmisApiAdaptor bean = new AmisApiAdaptor() {
            @Override
            public String getRequestAdaptor() {
                return null;
            }
        };
        AmisViewService service = createService("from-properties", null, List.of(bean));
        assertEquals("from-properties", service.resolveRequestAdaptor());
    }

    @Test
    void resolveRequestAdaptor_multipleBeans_firstNonNullWins() {
        AmisApiAdaptor bean1 = new AmisApiAdaptor() {
            @Override
            public String getRequestAdaptor() {
                return null;
            }
        };
        AmisApiAdaptor bean2 = new AmisApiAdaptor() {
            @Override
            public String getRequestAdaptor() {
                return "from-bean2; return api;";
            }
        };
        AmisViewService service = createService(List.of(bean1, bean2));
        assertEquals("from-bean2; return api;", service.resolveRequestAdaptor());
    }
}
