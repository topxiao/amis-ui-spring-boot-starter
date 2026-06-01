package com.github.topxiao.amisui;

import com.github.topxiao.amisui.ext.AmisSchemaProvider;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.List;
import java.util.Locale;

/**
 * 解析 {@code json:} 前缀视图名的 ViewResolver。
 * <p>
 * 遍历 {@link AmisSchemaProvider} chain，第一个返回非 null 的 schema
 * 包装为 {@link JsonView} 直接输出 JSON 响应。
 *
 * <pre>
 * 示例：
 *   json:users   → provider chain 解析 "users" → ClasspathAmisSchemaProvider 加载 users.json → 返回原始 JSON
 *   json:unknown → chain 中无匹配 → 返回 null，交给下一个 ViewResolver
 * </pre>
 */
public class JsonViewResolver implements ViewResolver, Ordered {

    private static final String PREFIX = "json:";

    private final List<AmisSchemaProvider> providers;
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public JsonViewResolver(List<AmisSchemaProvider> providers) {
        this.providers = providers != null ? providers : List.of();
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) {
        if (viewName == null || !viewName.startsWith(PREFIX)) {
            return null;
        }

        String name = viewName.substring(PREFIX.length());

        for (AmisSchemaProvider provider : providers) {
            String schema = provider.resolveSchema(name);
            if (schema != null) {
                return new JsonView(schema);
            }
        }

        return null;
    }
}
