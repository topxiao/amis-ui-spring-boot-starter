package com.github.topxiao.amisui.ext;

public interface AmisRenderInterceptor {
    default void beforeRender(AmisRenderContext context) {}
    default String afterRender(AmisRenderContext context, String html) { return html; }
}
