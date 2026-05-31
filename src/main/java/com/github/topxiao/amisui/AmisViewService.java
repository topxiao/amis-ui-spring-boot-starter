package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisPageCustomizer;
import com.github.topxiao.amisui.ext.AmisPropertiesCustomizer;
import com.github.topxiao.amisui.ext.AmisRenderContext;
import com.github.topxiao.amisui.ext.AmisRenderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Amis 渲染核心服务。
 * <p>
 * 收集所有扩展点（PropertiesCustomizer、PageCustomizer、RenderInterceptor），
 * 提供 HTML 渲染和 app 配置构建能力。实际渲染委托给 {@link AmisView}。
 */
public class AmisViewService {

    private static final Logger log = LoggerFactory.getLogger(AmisViewService.class);

    private final AmisProperties properties;
    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final List<AmisPropertiesCustomizer> propertiesCustomizers;
    private final List<AmisPageCustomizer> pageCustomizers;
    private final List<AmisRenderInterceptor> renderInterceptors;

    public AmisViewService(AmisProperties properties, Environment environment,
                           ObjectMapper objectMapper,
                           List<AmisPropertiesCustomizer> propertiesCustomizers,
                           List<AmisPageCustomizer> pageCustomizers,
                           List<AmisRenderInterceptor> renderInterceptors) {
        if (properties == null) {
            properties = new AmisProperties();
        }
        if (properties.getApp() == null) {
            properties.setApp(new AmisProperties.App());
        }
        this.properties = properties;
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.propertiesCustomizers = propertiesCustomizers != null ? propertiesCustomizers : List.of();
        this.pageCustomizers = pageCustomizers != null ? pageCustomizers : List.of();
        this.renderInterceptors = renderInterceptors != null ? renderInterceptors : List.of();
    }

    // -------------------------------------------------------------------------
    // Public accessors
    // -------------------------------------------------------------------------

    public AmisProperties getProperties() {
        return properties;
    }

    public Environment getEnvironment() {
        return environment;
    }

    /**
     * 获取 ObjectMapper（包内可见，供 AmisView 和 PropertiesAppSchemaProvider 使用）
     */
    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // -------------------------------------------------------------------------
    // Rendering — 委托给 AmisView
    // -------------------------------------------------------------------------

    /**
     * 渲染 app 模式 HTML（无自定义数据）
     */
    public String renderHtml() {
        return renderHtml(new HashMap<>());
    }

    /**
     * 渲染 app 模式 HTML（带自定义数据）
     */
    public String renderHtml(Map<String, Object> customData) {
        AmisView view = new AmisView(this, true);
        return view.renderToString(customData);
    }

    /**
     * 渲染 schema 模式 HTML（仅 schema JSON）
     */
    public String renderHtml(String schemaJson) {
        return renderHtml(schemaJson, "AMIS Page", null, null);
    }

    /**
     * 渲染 schema 模式 HTML（schema + 标题）
     */
    public String renderHtml(String schemaJson, String title) {
        return renderHtml(schemaJson, title, null, null);
    }

    /**
     * 渲染 schema 模式 HTML（完整参数）
     */
    public String renderHtml(String schemaJson, String title, String customCss, String customJs) {
        Map<String, Object> model = new HashMap<>();
        model.put("schema", schemaJson != null ? schemaJson : "{}");
        if (title != null) model.put("title", title);
        if (customCss != null) model.put("customCss", customCss);
        if (customJs != null) model.put("customJs", customJs);
        AmisView view = new AmisView(this, false);
        return view.renderToString(model);
    }

    // -------------------------------------------------------------------------
    // Extension / interceptor support
    // -------------------------------------------------------------------------

    /**
     * 依次执行所有 PropertiesCustomizer，返回最终配置
     */
    public AmisProperties applyPropertiesCustomizers() {
        AmisProperties result = properties;
        for (AmisPropertiesCustomizer customizer : propertiesCustomizers) {
            result = customizer.customize(result);
        }
        return result;
    }

    /**
     * 触发所有拦截器的 beforeRender 回调
     */
    public void invokeBeforeRenderInterceptors(AmisRenderContext context) {
        for (AmisRenderInterceptor interceptor : renderInterceptors) {
            try {
                interceptor.beforeRender(context);
            } catch (Exception e) {
                log.warn("Render interceptor '{}' failed in beforeRender, skipping",
                        interceptor.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 触发所有拦截器的 afterRender 回调，逐个修改 HTML
     */
    public String invokeAfterRenderInterceptors(AmisRenderContext context, String html) {
        String result = html;
        for (AmisRenderInterceptor interceptor : renderInterceptors) {
            try {
                result = interceptor.afterRender(context, result);
            } catch (Exception e) {
                log.warn("Render interceptor '{}' failed in afterRender, using previous result",
                        interceptor.getClass().getSimpleName(), e);
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // App configuration building
    // -------------------------------------------------------------------------

    /**
     * 根据 AmisProperties 构建 amis app 配置 Map。
     * <p>
     * 输出结构：
     * <pre>{ type, brandName, logo, header?, pages? }</pre>
     * 页面列表会经过所有 {@link AmisPageCustomizer} 处理后再转换为 Map。
     *
     * @param customizedProperties 经过 Customizer 处理后的配置
     * @return app 配置 Map，供序列化为 JSON
     */
    public Map<String, Object> buildAppConfig(AmisProperties customizedProperties) {
        Map<String, Object> app = new HashMap<>();
        app.put("type", "app");
        app.put("brandName", customizedProperties.getApp().getBrandName());
        app.put("logo", customizedProperties.getApp().getLogo());

        if (customizedProperties.getApp().getHeader() != null) {
            app.put("header", customizedProperties.getApp().getHeader());
        }

        log.debug("Pages configuration: {}", customizedProperties.getPages());

        if (customizedProperties.getPages() != null && !customizedProperties.getPages().isEmpty()) {
            List<AmisProperties.Page> pages = customizedProperties.getPages();
            log.debug("Found {} pages in configuration", pages.size());

            for (int i = 0; i < pages.size(); i++) {
                AmisProperties.Page page = pages.get(i);
                log.debug("Page[{}]: label={}, url={}, redirect={}, schemaApi={}, icon={}",
                        i, page.getLabel(), page.getUrl(), page.getRedirect(), page.getSchemaApi(), page.getIcon());
                if (page.getChildren() != null) {
                    log.debug("  Children count: {}", page.getChildren().size());
                    for (int j = 0; j < page.getChildren().size(); j++) {
                        AmisProperties.Page child = page.getChildren().get(j);
                        log.debug("  Child[{}]: label={}, url={}, schemaApi={}",
                                j, child.getLabel(), child.getUrl(), child.getSchemaApi());
                    }
                }
            }

            for (AmisPageCustomizer customizer : pageCustomizers) {
                pages = customizer.customizePages(pages, customizedProperties);
            }

            List<Map<String, Object>> convertedPages = pages.stream().map(this::convertPage).toList();
            log.debug("Converted pages: {}", convertedPages);
            app.put("pages", convertedPages);
        } else {
            log.warn("No pages configured or pages is null!");
        }

        return app;
    }

    /**
     * 将 Page 对象转换为 amis 需要的 Map 结构（仅包含非空字段）
     */
    private Map<String, Object> convertPage(AmisProperties.Page page) {
        Map<String, Object> pageMap = new HashMap<>();
        if (StringUtils.hasText(page.getLabel())) {
            pageMap.put("label", page.getLabel());
        }
        if (StringUtils.hasText(page.getUrl())) {
            pageMap.put("url", page.getUrl());
        }
        if (StringUtils.hasText(page.getRedirect())) {
            pageMap.put("redirect", page.getRedirect());
        }
        if (StringUtils.hasText(page.getSchemaApi())) {
            pageMap.put("schemaApi", page.getSchemaApi());
        }
        if (StringUtils.hasText(page.getLink())) {
            pageMap.put("link", page.getLink());
        }
        if (StringUtils.hasText(page.getIcon())) {
            pageMap.put("icon", page.getIcon());
        }
        if (page.getChildren() != null && !page.getChildren().isEmpty()) {
            pageMap.put("children", page.getChildren().stream().map(this::convertPage).toList());
        }
        return pageMap;
    }
}
