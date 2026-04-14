package com.github.topxiao.amisui;

import java.util.List;

public final class AmisPageFactory {

    private AmisPageFactory() {
    }

    /* ========== 普通页面 ========== */

    public static AmisUiProperties.Page page(
            String title,
            String path,
            String schemaApi,
            String icon,
            AmisUiProperties.Page[] children
    ) {
        AmisUiProperties.Page page = new AmisUiProperties.Page();
        page.setLabel(title);
        page.setUrl(path);
        page.setSchemaApi(schemaApi);
        page.setIcon(icon);
        page.setChildren(List.of(children));
        return page;
    }

    public static AmisUiProperties.Page page(
            String title,
            String path,
            String schemaApi,
            String icon
    ) {
        return page(title, path, schemaApi, icon, emptyChildren());
    }

    /* ========== 分组（Group） ========== */

    public static AmisUiProperties.Page group(
            String title,
            String icon,
            AmisUiProperties.Page[] children
    ) {
        AmisUiProperties.Page page = new AmisUiProperties.Page();
        page.setLabel(title);
        page.setIcon(icon);
        page.setChildren(List.of(children));
        return page;
    }

    /* ========== 辅助方法 ========== */

    public static AmisUiProperties.Page[] emptyChildren() {
        return new AmisUiProperties.Page[0];
    }

    public static AmisUiProperties.Page[] children(AmisUiProperties.Page... pages) {
        return pages == null ? emptyChildren() : pages;
    }
}
