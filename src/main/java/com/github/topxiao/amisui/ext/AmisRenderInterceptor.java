package com.github.topxiao.amisui.ext;

/**
 * Amis 渲染拦截器。
 * <p>
 * 在 HTML 渲染前后插入自定义逻辑，例如注入全局变量、修改 HTML 输出。
 * 注册为 Spring Bean 即可自动生效，多个拦截器按注入顺序依次执行。
 * <p>
 * 异常安全：单个拦截器抛异常不会中断渲染流程，仅跳过该拦截器。
 */
public interface AmisRenderInterceptor {

    /**
     * 渲染前回调。可以在此修改 context 中的数据。
     */
    default void beforeRender(AmisRenderContext context) {
    }

    /**
     * 渲染后回调。可以在此修改最终 HTML 输出。
     *
     * @param context 渲染上下文
     * @param html    渲染后的 HTML
     * @return 修改后的 HTML（不修改则直接返回入参）
     */
    default String afterRender(AmisRenderContext context, String html) {
        return html;
    }
}
