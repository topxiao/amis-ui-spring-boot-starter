package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisUiProperties;

import java.util.List;

/**
 * Amis UI 扩展点注册表
 * <p>
 * 用于管理和调度所有注册的扩展实现
 *
 * @author zyarn
 * @version 1.0.0
 */
public interface AmisUiExtensionRegistry {

    /**
     * 注册扩展实现
     *
     * @param extension 扩展实现
     */
    void registerExtension(AmisUiExtension extension);

    /**
     * 注册属性自定义器
     *
     * @param customizer 属性自定义器
     */
    void registerPropertiesCustomizer(AmisUiPropertiesCustomizer customizer);

    /**
     * 注册页面自定义器
     *
     * @param customizer 页面自定义器
     */
    void registerPageCustomizer(AmisUiPageCustomizer customizer);

    /**
     * 注册渲染拦截器
     *
     * @param interceptor 渲染拦截器
     */
    void registerRenderInterceptor(AmisUiRenderInterceptor interceptor);

    /**
     * 获取所有属性自定义器
     *
     * @return 属性自定义器列表，按优先级排序
     */
    List<AmisUiPropertiesCustomizer> getPropertiesCustomizers();

    /**
     * 获取所有渲染拦截器
     *
     * @return 渲染拦截器列表，按优先级排序
     */
    List<AmisUiRenderInterceptor> getRenderInterceptors();

    /**
     * 获取所有页面自定义器
     *
     * @return 页面自定义器列表，按优先级排序
     */
    List<AmisUiPageCustomizer> getPageCustomizers();

    /**
     * 应用所有属性自定义器
     *
     * @param properties 原始配置
     * @return 自定义后的配置
     */
    AmisUiProperties applyPropertiesCustomizers(AmisUiProperties properties);
}
