package com.github.topxiao.amisui;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

/**
 * Amis 自定义视图解析器，解析 amis: 前缀的视图名。
 * <p>
 * 支持：
 * - amis:app → app 模式（完整应用框架）
 * - amis:page → schema 单页模式
 * <p>
 * 其他视图名返回 null，交给下一个 ViewResolver 处理。
 */
public class AmisViewResolver implements ViewResolver, Ordered {

    private static final String PREFIX = "amis:";

    private final AmisViewService service;
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public AmisViewResolver(AmisViewService service) {
        this.service = service;
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

        String mode = viewName.substring(PREFIX.length());
        return switch (mode) {
            case "app" -> new AmisView(service, true);
            case "page" -> new AmisView(service, false);
            default -> null;
        };
    }
}
