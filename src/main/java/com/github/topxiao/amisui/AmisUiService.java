package com.github.topxiao.amisui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.topxiao.amisui.ext.AmisUiExtensionRegistry;
import com.github.topxiao.amisui.ext.AmisUiPageCustomizer;
import com.github.topxiao.amisui.ext.AmisUiRenderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Amis UI Service for rendering HTML templates.
 * <p>
 * Delegates rendering to {@link AmisView} while providing configuration,
 * extension registry, and interceptor management.
 */
public class AmisUiService {

    private static final Logger log = LoggerFactory.getLogger(AmisUiService.class);

    private final AmisUiProperties properties;
    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final AmisUiExtensionRegistry extensionRegistry;

    /**
     * Constructor injection — replaces the old setter-based approach.
     *
     * @param properties        configuration properties (null-safe: defaults to new AmisUiProperties)
     * @param environment       Spring environment (may be null)
     * @param objectMapper      Jackson ObjectMapper for JSON processing (must not be null)
     * @param extensionRegistry extension registry (may be null)
     */
    public AmisUiService(AmisUiProperties properties, Environment environment,
                         ObjectMapper objectMapper, AmisUiExtensionRegistry extensionRegistry) {
        // Default properties if not provided
        if (properties == null) {
            properties = new AmisUiProperties();
        }
        // Ensure app section exists
        if (properties.getApp() == null) {
            properties.setApp(new AmisUiProperties.App());
        }
        this.properties = properties;
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.extensionRegistry = extensionRegistry;
    }

    // -------------------------------------------------------------------------
    // Public accessors (kept for backward compatibility)
    // -------------------------------------------------------------------------

    public AmisUiProperties getProperties() {
        return properties;
    }

    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Package-private access to ObjectMapper for AmisView.
     */
    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public AmisUiExtensionRegistry getExtensionRegistry() {
        return extensionRegistry;
    }

    // -------------------------------------------------------------------------
    // Rendering — delegates to AmisView
    // -------------------------------------------------------------------------

    /**
     * Render the complete HTML page (app mode with default empty data).
     *
     * @return HTML string
     */
    public String renderHtml() {
        return renderHtml(new HashMap<>());
    }

    /**
     * Render HTML with custom data (app mode).
     *
     * @param customData custom data to merge
     * @return HTML string
     */
    public String renderHtml(Map<String, Object> customData) {
        AmisView view = new AmisView(this, true);
        return view.renderToString(customData);
    }

    /**
     * Render a single page using the provided schema JSON string.
     * This method generates a complete HTML page that embeds the Amis schema.
     *
     * @param schemaJson the Amis schema as a JSON string
     * @return the complete HTML page
     */
    public String renderHtml(String schemaJson) {
        return renderHtml(schemaJson, "AMIS Page", null, null);
    }

    /**
     * Render a single page using the provided schema JSON string with custom title.
     *
     * @param schemaJson the Amis schema as a JSON string
     * @param title      the page title
     * @return the complete HTML page
     */
    public String renderHtml(String schemaJson, String title) {
        return renderHtml(schemaJson, title, null, null);
    }

    /**
     * Render a single page with custom CSS and JS.
     *
     * @param schemaJson the Amis schema as a JSON string
     * @param title      the page title
     * @param customCss  custom CSS to inject into the page
     * @param customJs   custom JavaScript to inject into the page
     * @return the complete HTML page
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
     * Apply all registered properties customizers
     */
    public AmisUiProperties applyPropertiesCustomizers() {
        if (extensionRegistry != null) {
            return extensionRegistry.applyPropertiesCustomizers(properties);
        }
        return properties;
    }

    /**
     * Invoke all before render interceptors
     */
    public void invokeBeforeRenderInterceptors(AmisUiRenderInterceptor.RenderContext context) {
        if (extensionRegistry == null) {
            return;
        }
        List<AmisUiRenderInterceptor> interceptors = extensionRegistry.getRenderInterceptors();
        for (AmisUiRenderInterceptor interceptor : interceptors) {
            try {
                interceptor.beforeRender(context);
            } catch (Exception e) {
                log.warn("Render interceptor '{}' failed in beforeRender, skipping",
                        interceptor.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * Invoke all after render interceptors
     */
    public String invokeAfterRenderInterceptors(AmisUiRenderInterceptor.RenderContext context, String html) {
        if (extensionRegistry == null) {
            return html;
        }
        String result = html;
        List<AmisUiRenderInterceptor> interceptors = extensionRegistry.getRenderInterceptors();
        for (AmisUiRenderInterceptor interceptor : interceptors) {
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
     * Build app configuration with page customizers
     */
    public Map<String, Object> buildAppConfig(AmisUiProperties customizedProperties) {
        Map<String, Object> app = new HashMap<>();
        app.put("type", "app");
        app.put("brandName", customizedProperties.getApp().getBrandName());
        app.put("logo", customizedProperties.getApp().getLogo());

        if (customizedProperties.getApp().getHeader() != null) {
            app.put("header", customizedProperties.getApp().getHeader());
        }

        log.debug("Pages configuration: {}", customizedProperties.getPages());

        // Apply page customizers
        if (customizedProperties.getPages() != null && !customizedProperties.getPages().isEmpty()) {
            List<AmisUiProperties.Page> pages = customizedProperties.getPages();
            log.debug("Found {} pages in configuration", pages.size());

            // Print each page's details
            for (int i = 0; i < pages.size(); i++) {
                AmisUiProperties.Page page = pages.get(i);
                log.debug("Page[{}]: label={}, url={}, redirect={}, schemaApi={}, icon={}",
                        i, page.getLabel(), page.getUrl(), page.getRedirect(), page.getSchemaApi(), page.getIcon());
                if (page.getChildren() != null) {
                    log.debug("  Children count: {}", page.getChildren().size());
                    for (int j = 0; j < page.getChildren().size(); j++) {
                        AmisUiProperties.Page child = page.getChildren().get(j);
                        log.debug("  Child[{}]: label={}, url={}, schemaApi={}",
                                j, child.getLabel(), child.getUrl(), child.getSchemaApi());
                    }
                }
            }

            // Apply page customizers if registry is available
            if (extensionRegistry != null) {
                List<AmisUiPageCustomizer> pageCustomizers = extensionRegistry.getPageCustomizers();
                log.debug("Found {} page customizers", pageCustomizers.size());
                for (AmisUiPageCustomizer customizer : pageCustomizers) {
                    pages = customizer.customizePages(pages, customizedProperties);
                }
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
     * Convert Page configuration to map
     *
     * @param page page configuration
     * @return page map
     */
    private Map<String, Object> convertPage(AmisUiProperties.Page page) {
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
