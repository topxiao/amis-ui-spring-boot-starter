package com.github.topxiao.amisui.ext;

/**
 * Amis API 请求/响应拦截器扩展。
 * <p>
 * 返回 JS 函数体字符串，注入到 amis.embed() 的 env options 中。
 * 注册为 Spring Bean 即可自动生效。多个 Bean 按 {@code @Order} 排序，
 * 第一个返回非 null 的生效（first-non-null-wins）。
 * 返回 null 表示不提供该 adaptor，降级到配置属性或默认值。
 */
public interface AmisApiAdaptor {

    /**
     * 返回 requestAdaptor 的 JS 函数体。
     *
     * @return JS 函数体字符串，或 null 表示不提供
     */
    default String getRequestAdaptor() {
        return null;
    }

    /**
     * 返回 responseAdaptor 的 JS 函数体。
     *
     * @return JS 函数体字符串，或 null 表示不提供
     */
    default String getResponseAdaptor() {
        return null;
    }
}
