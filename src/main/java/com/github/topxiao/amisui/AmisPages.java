package com.github.topxiao.amisui;

import java.util.List;

/**
 * 页面定义工厂方法。
 * <p>
 * 提供 fluent 风格的静态方法来构建 {@link AmisProperties.Page}，简化配置类中的页面定义。
 * <p>
 * 用法示例：
 * <pre>
 * pages:
 *   - AmisPages.page("首页", "/home", "/api/schema/home", "fa fa-home")
 *   - AmisPages.group("系统管理", "fa fa-cog",
 *       AmisPages.children(
 *           AmisPages.page("用户管理", "/users", "/api/schema/users", "fa fa-user"),
 *           AmisPages.page("角色管理", "/roles", "/api/schema/roles", "fa fa-shield")
 *       ))
 * </pre>
 */
public final class AmisPages {

    private AmisPages() {
    }

    /* ========== 普通页面 ========== */

    /**
     * 创建带子菜单的页面
     */
    public static AmisProperties.Page page(
            String title,
            String path,
            String schemaApi,
            String icon,
            AmisProperties.Page[] children
    ) {
        AmisProperties.Page page = new AmisProperties.Page();
        page.setLabel(title);
        page.setUrl(path);
        page.setSchemaApi(schemaApi);
        page.setIcon(icon);
        page.setChildren(List.of(children));
        return page;
    }

    /**
     * 创建无子菜单的页面
     */
    public static AmisProperties.Page page(
            String title,
            String path,
            String schemaApi,
            String icon
    ) {
        return page(title, path, schemaApi, icon, emptyChildren());
    }

    /* ========== 分组（Group） ========== */

    /**
     * 创建菜单分组（无 url，仅作为子菜单容器）
     */
    public static AmisProperties.Page group(
            String title,
            String icon,
            AmisProperties.Page[] children
    ) {
        AmisProperties.Page page = new AmisProperties.Page();
        page.setLabel(title);
        page.setIcon(icon);
        page.setChildren(List.of(children));
        return page;
    }

    /* ========== 辅助方法 ========== */

    /**
     * 返回空子菜单数组
     */
    public static AmisProperties.Page[] emptyChildren() {
        return new AmisProperties.Page[0];
    }

    /**
     * 将多个 Page 打包为数组，null 安全
     */
    public static AmisProperties.Page[] children(AmisProperties.Page... pages) {
        return pages == null ? emptyChildren() : pages;
    }
}
