package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisProperties;

/**
 * Amis 配置属性定制器。
 * <p>
 * 在渲染前对 {@link AmisProperties} 进行二次加工，例如根据运行环境动态修改版本号、主题等。
 * 注册为 Spring Bean 即可自动生效，多个定制器按注入顺序依次执行。
 */
@FunctionalInterface
public interface AmisPropertiesCustomizer {

    /**
     * 定制配置属性。
     *
     * @param properties 原始配置
     * @return 修改后的配置（可以返回新对象或修改后返回同一对象）
     */
    AmisProperties customize(AmisProperties properties);
}
