package com.github.topxiao.amisui;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Amis UI Configuration Properties
 */
@ConfigurationProperties(prefix = "amis.ui")
public class AmisUiProperties {

    /**
     * 是否启用Amis UI
     */
    private boolean enabled = true;

    /**
     * Amis SDK版本
     */
    private String version = "6.12.0";

    /**
     * API主机地址
     */
    private String apiHost = "";

    /**
     * 上下文路径
     */
    private String ctx = "";

    /**
     * 应用配置
     */
    private App app = new App();

    /**
     * 页面配置
     */
    private List<Page> pages;

    /**
     * 自定义样式
     */
    private String customCss = "";

    /**
     * 自定义JavaScript
     */
    private String customJs = "";

    /**
     * 基础路径
     */
    private String basePath = "/amis";

    private static final Pattern SAFE_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9._\\-]+$");
    private static final Pattern SAFE_CTX_PATTERN = Pattern.compile("^/[a-zA-Z0-9/._\\-]*$");

    static void validateSafePath(String value, String fieldName) {
        if (value != null && !value.isEmpty() && !SAFE_PATH_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                fieldName + " contains invalid characters; only alphanumeric, dot, hyphen and underscore are allowed");
        }
    }

    private static void validateCtx(String value) {
        if (value != null && !value.isEmpty()) {
            if (value.contains("..") || !SAFE_CTX_PATTERN.matcher(value).matches()) {
                throw new IllegalArgumentException(
                    "ctx must start with '/' and contain only alphanumeric, slash, dot, hyphen and underscore");
            }
        }
    }

    public static class App {
        /**
         * 应用品牌名称
         */
        private String brandName = "Admin";

        /**
         * 应用Logo
         */
        private String logo = "img/logo.png";

        /**
         * 应用标题
         */
        private String title = "Admin";

        /**
         * 主题
         */
        private String theme = "ang";

        /**
         * 头部配置
         */
        private List<Map<String, Object>> header;

        // Getters and Setters
        public String getBrandName() {
            return brandName;
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
        }

        public String getLogo() {
            return logo;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            AmisUiProperties.validateSafePath(theme, "theme");
            this.theme = theme;
        }

        public List<Map<String, Object>> getHeader() {
            return header;
        }

        public void setHeader(List<Map<String, Object>> header) {
            this.header = header;
        }
    }

    public static class Page {
        /**
         * 页面标签
         */
        private String label;

        /**
         * 页面URL
         */
        private String url;

        /**
         * 重定向URL
         */
        private String redirect;

        /**
         * Schema API - 支持 schema-api (kebab-case) 和 schemaApi (camelCase)
         */
        @NestedConfigurationProperty
        private String schemaApi;

        /**
         * 外部链接
         */
        private String link;

        /**
         * 子页面
         */
        @NestedConfigurationProperty
        private List<Page> children;

        /**
         * 图标
         */
        private String icon;

        // Getters and Setters
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getRedirect() {
            return redirect;
        }

        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }

        public String getSchemaApi() {
            return schemaApi;
        }

        public void setSchemaApi(String schemaApi) {
            this.schemaApi = schemaApi;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public List<Page> getChildren() {
            return children;
        }

        public void setChildren(List<Page> children) {
            this.children = children;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        @Override
        public String toString() {
            return "Page{" +
                    "label='" + label + '\'' +
                    ", url='" + url + '\'' +
                    ", redirect='" + redirect + '\'' +
                    ", schemaApi='" + schemaApi + '\'' +
                    ", icon='" + icon + '\'' +
                    ", children=" + (children != null ? children.size() + " children" : "null") +
                    '}';
        }
    }

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        validateSafePath(version, "version");
        this.version = version;
    }

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getCtx() {
        return ctx;
    }

    public void setCtx(String ctx) {
        validateCtx(ctx);
        this.ctx = ctx;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public String getCustomCss() {
        return customCss;
    }

    public void setCustomCss(String customCss) {
        this.customCss = customCss;
    }

    public String getCustomJs() {
        return customJs;
    }

    public void setCustomJs(String customJs) {
        if (customJs != null && (customJs.contains("<script") || customJs.contains("</script"))) {
            throw new IllegalArgumentException(
                "customJs must not contain <script> tags; the starter already wraps it in a script block");
        }
        this.customJs = customJs;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
