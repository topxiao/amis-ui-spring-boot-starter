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
 * Core service for Amis view rendering.
 * <p>
 * Collects extensions via Spring constructor injection and delegates
 * rendering to {@link AmisView}.
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

    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // -------------------------------------------------------------------------
    // Rendering — delegates to AmisView
    // -------------------------------------------------------------------------

    public String renderHtml() {
        return renderHtml(new HashMap<>());
    }

    public String renderHtml(Map<String, Object> customData) {
        AmisView view = new AmisView(this, true);
        return view.renderToString(customData);
    }

    public String renderHtml(String schemaJson) {
        return renderHtml(schemaJson, "AMIS Page", null, null);
    }

    public String renderHtml(String schemaJson, String title) {
        return renderHtml(schemaJson, title, null, null);
    }

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

    public AmisProperties applyPropertiesCustomizers() {
        AmisProperties result = properties;
        for (AmisPropertiesCustomizer customizer : propertiesCustomizers) {
            result = customizer.customize(result);
        }
        return result;
    }

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
