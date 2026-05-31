package com.github.topxiao.amisui;

import com.github.topxiao.amisui.ext.AmisSchemaProvider;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.List;
import java.util.Locale;

/**
 * Amis view resolver that resolves {@code amis:} prefixed view names.
 * <p>
 * Resolution rules:
 * <ul>
 *   <li>{@code amis:app} → app mode (full multi-page framework)</li>
 *   <li>{@code amis:xxx} → iterates {@link AmisSchemaProvider} chain;
 *       first non-null result renders as schema page</li>
 * </ul>
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

    @Override
    public View resolveViewName(String viewName, Locale locale) {
        if (viewName == null || !viewName.startsWith(PREFIX)) {
            return null;
        }

        String name = viewName.substring(PREFIX.length());

        if ("app".equals(name)) {
            return new AmisView(service, true);
        }

        for (AmisSchemaProvider provider : providers) {
            String schema = provider.resolveSchema(name);
            if (schema != null) {
                return new AmisView(service, false, schema);
            }
        }

        return null;
    }
}
