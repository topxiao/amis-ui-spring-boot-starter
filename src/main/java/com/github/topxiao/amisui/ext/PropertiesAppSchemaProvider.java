package com.github.topxiao.amisui.ext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.AmisViewService;

import java.util.Map;

/**
 * 从 {@link com.github.topxiao.amisui.AmisProperties} 构建 app 配置的 Schema 解析器。
 * <p>
 * 仅处理 name="app"，将其余名称返回 null。
 * 调用 {@link AmisViewService#buildAppConfig} 构建配置 Map，再序列化为 JSON 字符串。
 * <p>
 * 作为 provider chain 的兜底实现（优先级最低），classpath 中如果存在 app.json 会优先使用。
 */
public class PropertiesAppSchemaProvider implements AmisSchemaProvider {

    private final AmisViewService service;
    private final ObjectMapper objectMapper;

    public PropertiesAppSchemaProvider(AmisViewService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @Override
    public String resolveSchema(String name) {
        if (!"app".equals(name)) {
            return null;
        }
        Map<String, Object> config = service.buildAppConfig(service.applyPropertiesCustomizers());
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize app configuration", e);
        }
    }
}
