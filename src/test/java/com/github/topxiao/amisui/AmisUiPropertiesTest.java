package com.github.topxiao.amisui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmisUiPropertiesTest {

    @Test
    void defaultValuesAreCorrect() {
        AmisUiProperties props = new AmisUiProperties();
        assertTrue(props.isEnabled());
        assertEquals("6.12.0", props.getVersion());
        assertEquals("ang", props.getApp().getTheme());
        assertEquals("/amis", props.getBasePath());
    }

    @Test
    void setVersion_acceptsValidVersion() {
        AmisUiProperties props = new AmisUiProperties();
        props.setVersion("6.12.0");
        assertEquals("6.12.0", props.getVersion());
    }

    @Test
    void setVersion_rejectsPathTraversal() {
        AmisUiProperties props = new AmisUiProperties();
        assertThrows(IllegalArgumentException.class, () -> props.setVersion("../../etc"));
    }

    @Test
    void setVersion_rejectsSlashInVersion() {
        AmisUiProperties props = new AmisUiProperties();
        assertThrows(IllegalArgumentException.class, () -> props.setVersion("6/12/0"));
    }

    @Test
    void setVersion_acceptsNull() {
        AmisUiProperties props = new AmisUiProperties();
        props.setVersion(null);
        assertNull(props.getVersion());
    }

    @Test
    void setTheme_acceptsValidTheme() {
        AmisUiProperties props = new AmisUiProperties();
        props.getApp().setTheme("ang");
        assertEquals("ang", props.getApp().getTheme());
    }

    @Test
    void setTheme_rejectsPathTraversal() {
        AmisUiProperties props = new AmisUiProperties();
        assertThrows(IllegalArgumentException.class, () -> props.getApp().setTheme("../evil"));
    }

    @Test
    void setCtx_acceptsValidContextPath() {
        AmisUiProperties props = new AmisUiProperties();
        props.setCtx("/amis-ui");
        assertEquals("/amis-ui", props.getCtx());
    }

    @Test
    void setCtx_acceptsEmptyString() {
        AmisUiProperties props = new AmisUiProperties();
        props.setCtx("");
        assertEquals("", props.getCtx());
    }

    @Test
    void setCtx_rejectsPathTraversal() {
        AmisUiProperties props = new AmisUiProperties();
        assertThrows(IllegalArgumentException.class, () -> props.setCtx("/../../etc"));
    }

    @Test
    void setCtx_rejectsNoLeadingSlash() {
        AmisUiProperties props = new AmisUiProperties();
        assertThrows(IllegalArgumentException.class, () -> props.setCtx("amis-ui"));
    }

    @Test
    void setCustomJs_acceptsNormalScript() {
        AmisUiProperties props = new AmisUiProperties();
        props.setCustomJs("console.log('hello')");
        assertEquals("console.log('hello')", props.getCustomJs());
    }

    @Test
    void setCustomJs_rejectsScriptTag() {
        AmisUiProperties props = new AmisUiProperties();
        assertThrows(IllegalArgumentException.class,
            () -> props.setCustomJs("<script>alert('xss')</script>"));
    }

    @Test
    void setCustomJs_acceptsNull() {
        AmisUiProperties props = new AmisUiProperties();
        props.setCustomJs(null);
        assertNull(props.getCustomJs());
    }
}
