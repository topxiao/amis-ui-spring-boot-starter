package com.github.topxiao.amisui;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "amis")
public class AmisProperties {

    private boolean enabled = true;
    private String version = "6.12.0";
    private String ctx = "";
    private App app = new App();
    private List<Page> pages;
    private String path = "/amis";

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

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getVersion() { return version; }
    public void setVersion(String version) { validateSafePath(version, "version"); this.version = version; }

    public String getCtx() { return ctx; }
    public void setCtx(String ctx) { validateCtx(ctx); this.ctx = ctx; }

    public App getApp() { return app; }
    public void setApp(App app) { this.app = app; }

    public List<Page> getPages() { return pages; }
    public void setPages(List<Page> pages) { this.pages = pages; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public static class App {
        private String brandName = "Admin";
        private String logo = "img/logo.png";
        private String title = "Admin";
        private String theme = "ang";
        private List<Map<String, Object>> header;

        public String getBrandName() { return brandName; }
        public void setBrandName(String brandName) { this.brandName = brandName; }
        public String getLogo() { return logo; }
        public void setLogo(String logo) { this.logo = logo; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getTheme() { return theme; }
        public void setTheme(String theme) { validateSafePath(theme, "theme"); this.theme = theme; }
        public List<Map<String, Object>> getHeader() { return header; }
        public void setHeader(List<Map<String, Object>> header) { this.header = header; }
    }

    public static class Page {
        private String label;
        private String url;
        private String redirect;
        private String schemaApi;
        private String link;
        private List<Page> children;
        private String icon;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getRedirect() { return redirect; }
        public void setRedirect(String redirect) { this.redirect = redirect; }
        public String getSchemaApi() { return schemaApi; }
        public void setSchemaApi(String schemaApi) { this.schemaApi = schemaApi; }
        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
        public List<Page> getChildren() { return children; }
        public void setChildren(List<Page> children) { this.children = children; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        @Override
        public String toString() {
            return "Page{label='" + label + "', url='" + url + "', redirect='" + redirect
                + "', schemaApi='" + schemaApi + "', icon='" + icon
                + "', children=" + (children != null ? children.size() + " children" : "null") + '}';
        }
    }
}
