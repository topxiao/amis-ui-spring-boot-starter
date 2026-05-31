package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisProperties;

import java.util.List;

/**
 * Amis 页面列表定制器。
 * <p>
 * 在构建 app 配置前对页面列表进行动态调整，例如根据权限过滤页面、动态添加菜单项。
 * 注册为 Spring Bean 即可自动生效，多个定制器按注入顺序依次执行。
 */
@FunctionalInterface
public interface AmisPageCustomizer {

    /**
     * 定制页面列表。
     *
     * @param pages      当前页面列表
     * @param properties 当前配置属性（经过 PropertiesCustomizer 处理后的）
     * @return 修改后的页面列表
     */
    List<AmisProperties.Page> customizePages(List<AmisProperties.Page> pages, AmisProperties properties);
}
