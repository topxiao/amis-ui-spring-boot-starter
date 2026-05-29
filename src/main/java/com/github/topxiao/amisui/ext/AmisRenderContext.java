package com.github.topxiao.amisui.ext;

import java.util.Map;

public class AmisRenderContext {
    private final Map<String, Object> data;
    private final String templateType;

    public AmisRenderContext(Map<String, Object> data, String templateType) {
        this.data = data;
        this.templateType = templateType;
    }

    public Map<String, Object> getData() { return data; }
    public String getTemplateType() { return templateType; }
    public Object get(String key) { return data.get(key); }
    public void set(String key, Object value) { data.put(key, value); }
}
