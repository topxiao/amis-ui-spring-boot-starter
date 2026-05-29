package com.github.topxiao.amisui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmisPropertiesTest {

    @Test
    void defaultValuesAreCorrect() {
        AmisProperties props = new AmisProperties();
        assertTrue(props.isEnabled());
        assertEquals("6.12.0", props.getVersion());
        assertEquals("ang", props.getApp().getTheme());
        assertEquals("/amis", props.getPath());
    }

    @Test
    void path_defaultsToAmis() {
        AmisProperties props = new AmisProperties();
        assertEquals("/amis", props.getPath());
    }

    @Test
    void setPath_acceptsValidPath() {
        AmisProperties props = new AmisProperties();
        props.setPath("/admin");
        assertEquals("/admin", props.getPath());
    }

    @Test
    void setVersion_acceptsValidVersion() {
        AmisProperties props = new AmisProperties();
        props.setVersion("6.12.0");
        assertEquals("6.12.0", props.getVersion());
    }

    @Test
    void setVersion_rejectsPathTraversal() {
        AmisProperties props = new AmisProperties();
        assertThrows(IllegalArgumentException.class, () -> props.setVersion("../../etc"));
    }

    @Test
    void setVersion_rejectsSlashInVersion() {
        AmisProperties props = new AmisProperties();
        assertThrows(IllegalArgumentException.class, () -> props.setVersion("6/12/0"));
    }

    @Test
    void setVersion_acceptsNull() {
        AmisProperties props = new AmisProperties();
        props.setVersion(null);
        assertNull(props.getVersion());
    }

    @Test
    void setTheme_acceptsValidTheme() {
        AmisProperties props = new AmisProperties();
        props.getApp().setTheme("ang");
        assertEquals("ang", props.getApp().getTheme());
    }

    @Test
    void setTheme_rejectsPathTraversal() {
        AmisProperties props = new AmisProperties();
        assertThrows(IllegalArgumentException.class, () -> props.getApp().setTheme("../evil"));
    }

    @Test
    void setCtx_acceptsValidContextPath() {
        AmisProperties props = new AmisProperties();
        props.setCtx("/amis-ui");
        assertEquals("/amis-ui", props.getCtx());
    }

    @Test
    void setCtx_acceptsEmptyString() {
        AmisProperties props = new AmisProperties();
        props.setCtx("");
        assertEquals("", props.getCtx());
    }

    @Test
    void setCtx_rejectsPathTraversal() {
        AmisProperties props = new AmisProperties();
        assertThrows(IllegalArgumentException.class, () -> props.setCtx("/../../etc"));
    }

    @Test
    void setCtx_rejectsNoLeadingSlash() {
        AmisProperties props = new AmisProperties();
        assertThrows(IllegalArgumentException.class, () -> props.setCtx("amis-ui"));
    }
}
