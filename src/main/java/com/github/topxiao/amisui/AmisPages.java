package com.github.topxiao.amisui;

import java.util.List;

public final class AmisPages {

    private AmisPages() {
    }

    /* ========== 普通页面 ========== */

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

    public static AmisProperties.Page page(
            String title,
            String path,
            String schemaApi,
            String icon
    ) {
        return page(title, path, schemaApi, icon, emptyChildren());
    }

    /* ========== 分组（Group） ========== */

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

    public static AmisProperties.Page[] emptyChildren() {
        return new AmisProperties.Page[0];
    }

    public static AmisProperties.Page[] children(AmisProperties.Page... pages) {
        return pages == null ? emptyChildren() : pages;
    }
}
