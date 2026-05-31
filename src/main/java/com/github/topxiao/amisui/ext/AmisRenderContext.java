package com.github.topxiao.amisui.ext;

import java.util.Map;

/**
 * Amis 渲染上下文。
 * <p>
 * 在 {@link AmisRenderInterceptor} 中传递的上下文对象，包含渲染数据和模板类型信息。
 */
public class AmisRenderContext {

    private final Map<String, Object> data;
    /**
     * 模板类型："app" 或 "schema"
     */
    private final String templateType;

    public AmisRenderContext(Map<String, Object> data, String templateType) {
        this.data = data;
        this.templateType = templateType;
    }

    /**
     * 获取渲染数据 Map
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * 获取模板类型：{@code "app"} 或 {@code "schema"}
     */
    public String getTemplateType() {
        return templateType;
    }

    /**
     * 从数据中取值
     */
    public Object get(String key) {
        return data.get(key);
    }

    /**
     * 向数据中写入值，可在拦截器中注入额外数据
     */
    public void set(String key, Object value) {
        data.put(key, value);
    }
}
