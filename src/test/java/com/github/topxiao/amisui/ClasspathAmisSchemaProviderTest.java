package com.github.topxiao.amisui;

import com.github.topxiao.amisui.ext.ClasspathAmisSchemaProvider;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class ClasspathAmisSchemaProviderTest {

    private final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    @Test
    void resolveSchema_fileExists_returnsContent() {
        var provider = new ClasspathAmisSchemaProvider(resourceLoader, "classpath:amis/", true);

        String schema = provider.resolveSchema("users");

        assertThat(schema).contains("\"type\":\"page\"");
        assertThat(schema).contains("\"title\":\"Users\"");
    }

    @Test
    void resolveSchema_subdirectoryFile_returnsContent() {
        var provider = new ClasspathAmisSchemaProvider(resourceLoader, "classpath:amis/", true);

        String schema = provider.resolveSchema("system/roles");

        assertThat(schema).contains("\"type\":\"page\"");
        assertThat(schema).contains("\"title\":\"Roles\"");
    }

    @Test
    void resolveSchema_fileNotExists_returnsNull() {
        var provider = new ClasspathAmisSchemaProvider(resourceLoader, "classpath:amis/", true);

        String schema = provider.resolveSchema("nonexistent");

        assertThat(schema).isNull();
    }

    @Test
    void resolveSchema_cacheEnabled_returnsSameInstance() {
        var provider = new ClasspathAmisSchemaProvider(resourceLoader, "classpath:amis/", true);

        String first = provider.resolveSchema("users");
        String second = provider.resolveSchema("users");

        assertThat(first).isSameAs(second);
    }

    @Test
    void resolveSchema_cacheDisabled_readsEachTime() {
        var provider = new ClasspathAmisSchemaProvider(resourceLoader, "classpath:amis/", false);

        String first = provider.resolveSchema("users");
        String second = provider.resolveSchema("users");

        assertThat(first).isEqualTo(second);
        // Content equal but not guaranteed same instance without cache
        assertThat(first).contains("\"type\":\"page\"");
    }

    @Test
    void resolveSchema_customPrefix_works() {
        var provider = new ClasspathAmisSchemaProvider(resourceLoader, "classpath:amis/system/", true);

        String schema = provider.resolveSchema("roles");

        assertThat(schema).contains("\"title\":\"Roles\"");
    }
}
