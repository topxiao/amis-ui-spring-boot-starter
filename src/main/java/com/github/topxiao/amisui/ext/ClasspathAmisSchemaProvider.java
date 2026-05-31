package com.github.topxiao.amisui.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 从 classpath 加载 JSON Schema 文件的默认实现。
 * <p>
 * 拼接规则：{@code schemaPrefix + name + ".json"}，例如：
 * <ul>
 *   <li>name="users", prefix="classpath:amis/" → 加载 classpath:amis/users.json</li>
 *   <li>name="system/roles", prefix="classpath:amis/" → 加载 classpath:amis/system/roles.json</li>
 * </ul>
 * 支持可选的内存缓存（{@link ConcurrentHashMap}）。
 */
public class ClasspathAmisSchemaProvider implements AmisSchemaProvider {

    private static final Logger log = LoggerFactory.getLogger(ClasspathAmisSchemaProvider.class);

    private final ResourceLoader resourceLoader;
    private final String schemaPrefix;
    private final boolean cacheEnabled;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public ClasspathAmisSchemaProvider(ResourceLoader resourceLoader,
                                       String schemaPrefix, boolean cacheEnabled) {
        this.resourceLoader = resourceLoader;
        this.schemaPrefix = schemaPrefix;
        this.cacheEnabled = cacheEnabled;
    }

    @Override
    public String resolveSchema(String name) {
        if (cacheEnabled) {
            return cache.computeIfAbsent(name, this::loadFromDisk);
        }
        return loadFromDisk(name);
    }

    /**
     * 按 schemaPrefix + name + ".json" 路径加载文件内容
     */
    private String loadFromDisk(String name) {
        String location = schemaPrefix + name + ".json";
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (Exception e) {
            log.warn("Failed to load schema from {}: {}", location, e.getMessage());
            return null;
        }
    }
}
