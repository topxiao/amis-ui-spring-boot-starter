package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisUiProperties;

/**
 * Amis UI 属性配置自定义器接口
 * <p>
 * 引用方可以实现此接口来自定义修改AmisUiProperties配置
 * 在配置加载完成后、渲染之前会被调用
 * <p>
 * 使用场景：
 * - 根据运行环境动态修改配置
 * - 从外部配置源加载配置
 * - 配置值的计算和转换
 * - 配置验证和默认值设置
 *
 * @author zyarn
 * @version 1.0.0
 */
public interface AmisUiPropertiesCustomizer {

    /**
     * 获取扩展点名称
     *
     * @return 扩展点名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取扩展点优先级
     * 数值越小，优先级越高
     *
     * @return 优先级，默认为0
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 自定义AmisUiProperties配置
     *
     * @param properties 原始配置属性
     * @return 自定义后的配置属性
     */
    AmisUiProperties customize(AmisUiProperties properties);


}

