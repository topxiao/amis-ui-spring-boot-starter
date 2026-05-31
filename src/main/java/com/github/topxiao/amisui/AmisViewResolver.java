package com.github.topxiao.amisui;

import com.github.topxiao.amisui.ext.AmisSchemaProvider;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.List;
import java.util.Locale;

/**
 * 解析 {@code amis:} 前缀视图名的 ViewResolver。
 * <p>
 * 解析流程：所有名称（包括 "app"）统一通过 {@link AmisSchemaProvider} chain 解析，
 * 按顺序调用，第一个返回非 null 结果的 provider 生效。
 * <p>
 * 渲染模板由视图名称决定，与数据来源无关：
 * <ul>
 *   <li>{@code amis:app} → 使用多页面框架模板（hash 路由、导航栏）</li>
 *   <li>{@code amis:xxx} → 使用单页面 Schema 模板</li>
 * </ul>
 *
 * <pre>
 * 示例：
 *   amis:app     → provider chain 解析 "app" → ClasspathAmisSchemaProvider 或 PropertiesAppSchemaProvider
 *   amis:users   → provider chain 解析 "users" → ClasspathAmisSchemaProvider 加载 users.json
 *   amis:unknown → chain 中无匹配 → 返回 null，交给下一个 ViewResolver
 * </pre>
 */
public class AmisViewResolver implements ViewResolver, Ordered {

    private static final String PREFIX = "amis:";

    private final AmisViewService service;
    private final List<AmisSchemaProvider> providers;
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public AmisViewResolver(AmisViewService service, List<AmisSchemaProvider> providers) {
        this.service = service;
        this.providers = providers != null ? providers : List.of();
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * 解析视图名。仅处理 {@code "amis:"} 前缀的名称。
     * <p>
     * 遍历 provider chain，第一个返回非 null 的 schema 作为渲染数据。
     * "app" 名称使用 appMode 模板，其余使用 schema 模板。
     */
    @Override
    public View resolveViewName(String viewName, Locale locale) {
        if (viewName == null || !viewName.startsWith(PREFIX)) {
            return null;
        }

        String name = viewName.substring(PREFIX.length());

        for (AmisSchemaProvider provider : providers) {
            String schema = provider.resolveSchema(name);
            if (schema != null) {
                return new AmisView(service, "app".equals(name), schema);
            }
        }

        return null;
    }
}
