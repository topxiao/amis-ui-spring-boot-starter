package com.github.topxiao.amisui;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Amis Starter 配置属性，前缀 {@code amis}。
 * <p>
 * 在 application.yml 中配置，例如：
 * <pre>
 * amis:
 *   enabled: true
 *   version: "6.12.0"
 *   path: /amis
 *   ctx: /
 *   schema-prefix: "classpath:amis/"
 *   cache-enabled: true
 *   app:
 *     brand-name: "My App"
 *     logo: "img/logo.png"
 *     title: "My App"
 *     theme: "ang"
 *   pages:
 *     - label: "首页"
 *       url: "/home"
 *       schema-api: "/api/schema/home"
 *       icon: "fa fa-home"
 * </pre>
 */
@ConfigurationProperties(prefix = "amis")
public class AmisProperties {

    private boolean enabled = true;
    /**
     * amis SDK 版本号，用于拼接 CDN 路径
     */
    private String version = "6.12.0";
    /**
     * 上下文路径前缀，用于拼接 CDN 路径，默认读取 server.servlet.context-path
     */
    private String ctx = "";
    /**
     * App 全局配置（品牌名、Logo、主题等）
     */
    private App app = new App();
    /**
     * 页面列表，对应 amis app 的 pages 配置
     */
    private List<Page> pages;
    /**
     * 默认访问路径，注册为 ViewController
     */
    private String path = "/amis";
    /**
     * Schema 文件的 classpath 前缀，供 ClasspathAmisSchemaProvider 使用
     */
    private String schemaPrefix = "classpath:amis/";
    /**
     * 是否启用 Schema 文件内存缓存
     */
    private boolean cacheEnabled = true;
    /**
     * Schema API 端点基础路径，访问 {schemaPath}/** 返回 classpath 对应 .json 文件内容
     */
    private String schemaPath = "/schema";
    /**
     * 是否启用 Schema API 端点
     */
    private boolean schemaEnabled = true;
    /**
     * requestAdaptor JS 函数体，注入到 amis.embed() 的 env options。
     */
    private String requestAdaptor;
    /**
     * responseAdaptor JS 函数体，注入到 amis.embed() 的 env options。
     */
    private String responseAdaptor;

    private static final Pattern SAFE_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9._\\-]+$");
    private static final Pattern SAFE_CTX_PATTERN = Pattern.compile("^/[a-zA-Z0-9/._\\-]*$");
    private static final Pattern SAFE_SCHEMA_PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/._\\-]+$");

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSchemaPrefix() {
        return schemaPrefix;
    }

    public void setSchemaPrefix(String schemaPrefix) {
        this.schemaPrefix = schemaPrefix;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        if (schemaPath != null && !SAFE_SCHEMA_PATH_PATTERN.matcher(schemaPath).matches()) {
            throw new IllegalArgumentException(
                    "schemaPath must start with '/' and contain only alphanumeric, slash, dot, hyphen and underscore");
        }
        this.schemaPath = schemaPath;
    }

    public boolean isSchemaEnabled() {
        return schemaEnabled;
    }

    public void setSchemaEnabled(boolean schemaEnabled) {
        this.schemaEnabled = schemaEnabled;
    }

    public String getRequestAdaptor() {
        return requestAdaptor;
    }

    public void setRequestAdaptor(String requestAdaptor) {
        this.requestAdaptor = requestAdaptor;
    }

    public String getResponseAdaptor() {
        return responseAdaptor;
    }

    public void setResponseAdaptor(String responseAdaptor) {
        this.responseAdaptor = responseAdaptor;
    }

    /**
     * App 全局配置。
     * <p>
     * 对应 amis app 顶层的 brandName、logo、header 等字段。
     */
    public static class App {
        private String brandName = "Admin";
        private String logo = "img/logo.png";
        /**
         * HTML 页面标题
         */
        private String title = "Admin";
        /**
         * amis 主题（ang/cde等）
         */
        private String theme = "ang";
        /**
         * 自定义 header 区域配置
         */
        private List<Map<String, Object>> header;

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
            validateSafePath(theme, "theme");
            this.theme = theme;
        }

        public List<Map<String, Object>> getHeader() {
            return header;
        }

        public void setHeader(List<Map<String, Object>> header) {
            this.header = header;
        }
    }

    /**
     * 页面配置。
     * <p>
     * 对应 amis app pages 数组中的每一项，支持嵌套 children 实现多级菜单。
     */
    public static class Page {
        private String label;
        private String url;
        private String redirect;
        /**
         * 动态加载 schema 的 API 地址
         */
        private String schemaApi;
        /**
         * 外部链接
         */
        private String link;
        /**
         * 子菜单
         */
        private List<Page> children;
        /**
         * 图标类名
         */
        private String icon;

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
            return "Page{label='" + label + "', url='" + url + "', redirect='" + redirect
                    + "', schemaApi='" + schemaApi + "', icon='" + icon
                    + "', children=" + (children != null ? children.size() + " children" : "null") + '}';
        }
    }
}
