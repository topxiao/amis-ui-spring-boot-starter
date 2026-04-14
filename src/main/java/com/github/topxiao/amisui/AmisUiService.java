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
import java.util.Optional;

/**
 * Amis UI Service for rendering HTML templates
 */
public class AmisUiService {

    private static final Logger log = LoggerFactory.getLogger(AmisUiService.class);

    private AmisUiProperties properties;

    private Environment environment;

    private ObjectMapper objectMapper;

    private AmisUiExtensionRegistry extensionRegistry;

    // Setters for dependency injection
    public void setProperties(AmisUiProperties properties) {
        this.properties = properties;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setExtensionRegistry(AmisUiExtensionRegistry extensionRegistry) {
        this.extensionRegistry = extensionRegistry;
    }

    public AmisUiExtensionRegistry getExtensionRegistry() {
        return extensionRegistry;
    }

    /**
     * Render the complete HTML page
     *
     * @return HTML string
     */
    public String renderHtml() {
        return renderHtml(new HashMap<>());
    }

    /**
     * Render HTML with custom data
     *
     * @param customData custom data to merge
     * @return HTML string
     */
    public String renderHtml(Map<String, Object> customData) {
        // Apply properties customizers first
        AmisUiProperties customizedProperties = applyPropertiesCustomizers();

        // Build render context
        AmisUiRenderInterceptor.RenderContext context =
                new AmisUiRenderInterceptor.RenderContext(customData, "default");

        // Before render interceptors
        invokeBeforeRenderInterceptors(context);

        // Build app configuration with page customizers
        Map<String, Object> appConfig = buildAppConfig(customizedProperties);
        context.set("appConfig", appConfig);

        // Generate HTML
        String html = processTemplate(getHtmlTemplate(), appConfig, customizedProperties);

        // After render interceptors
        html = invokeAfterRenderInterceptors(context, html);

        return html;
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
        AmisUiProperties customizedProperties = applyPropertiesCustomizers();
        String version = customizedProperties.getVersion();
        String ctx = Optional.ofNullable(customizedProperties.getCtx())
                .filter(StringUtils::hasText)
                .orElseGet(() -> Optional.ofNullable(properties.getCtx())
                        .filter(StringUtils::hasText)
                        .orElse(environment.getProperty("server.servlet.context-path", "")));
        String theme = Optional.ofNullable(customizedProperties.getApp().getTheme())
                .filter(StringUtils::hasText)
                .orElseGet(() -> Optional.ofNullable(properties.getApp().getTheme()).orElse("ang"));

        String html = getSchemaPageTemplate()
                .replace("${title}", title != null ? title : "AMIS Page")
                .replace("${version}", version)
                .replace("${schemaJson}", schemaJson != null ? schemaJson : "{}")
                .replace("${ctx}", ctx)
                .replace("${theme}", theme)
                .replace("${customCss}", customCss != null ? customCss : "")
                .replace("${customJs}", customJs != null ? customJs : "");

        // Apply after render interceptors
        AmisUiRenderInterceptor.RenderContext context =
                new AmisUiRenderInterceptor.RenderContext(new HashMap<>(), "schema");
        return invokeAfterRenderInterceptors(context, html);
    }

    /**
     * Apply all registered properties customizers
     */
    private AmisUiProperties applyPropertiesCustomizers() {
        if (extensionRegistry != null) {
            return extensionRegistry.applyPropertiesCustomizers(properties);
        }
        return properties;
    }

    /**
     * Invoke all before render interceptors
     */
    private void invokeBeforeRenderInterceptors(AmisUiRenderInterceptor.RenderContext context) {
        if (extensionRegistry == null) {
            return;
        }
        List<AmisUiRenderInterceptor> interceptors = extensionRegistry.getRenderInterceptors();
        for (AmisUiRenderInterceptor interceptor : interceptors) {
            interceptor.beforeRender(context);
        }
    }

    /**
     * Invoke all after render interceptors
     */
    private String invokeAfterRenderInterceptors(AmisUiRenderInterceptor.RenderContext context, String html) {
        if (extensionRegistry == null) {
            return html;
        }
        String result = html;
        List<AmisUiRenderInterceptor> interceptors = extensionRegistry.getRenderInterceptors();
        for (AmisUiRenderInterceptor interceptor : interceptors) {
            result = interceptor.afterRender(context, result);
        }
        return result;
    }

    /**
     * Build app configuration with page customizers
     */
    private Map<String, Object> buildAppConfig(AmisUiProperties customizedProperties) {
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
     * Get the base HTML template
     *
     * @return HTML template string
     */
    private String getHtmlTemplate() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <title>${title}</title>
                    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1"/>
                    <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
                    <link rel="icon" href="/img/logo.png">
                    <link rel="stylesheet" title="default" href="${ctx}/cdn/amis/${version}/sdk.css"/>
                    <link rel="stylesheet" href="${ctx}/cdn/amis/${version}/helper.css"/>
                    <link rel="stylesheet" href="${ctx}/cdn/amis/${version}/iconfont.css"/>
                    <link rel="stylesheet" href="${ctx}/cdn/amis/${version}/${theme}.css"/>
                    <script src="${ctx}/cdn/amis/${version}/sdk.js"></script>
                    <script src="${ctx}/cdn/amis/${version}/umd/history.js"></script>
                    <style>
                        html,
                        body,
                        .app-wrapper {
                            position: relative;
                            width: 100%;
                            height: 100%;
                            margin: 0;
                            padding: 0;
                        }
                        ${customCss}
                    </style>
                </head>
                <body>
                <div id="root" class="app-wrapper"></div>
                <script>
                    (function () {
                        let amis = amisRequire('amis/embed');
                        const match = amisRequire('path-to-regexp').match;
                        const history = amisRequire('history').createHashHistory({basename: ''});
                        const API_HOST = '${apiHost}';
                
                        const app = ${appJson};
                
                        function normalizeLink(to, location = history.location) {
                            to = to || '';
                            if (to && to[0] === '#') {
                                to = location.pathname + location.search + to;
                            } else if (to && to[0] === '?') {
                                to = location.pathname + to;
                            }
                
                            const idx = to.indexOf('?');
                            const idx2 = to.indexOf('#');
                            let pathname = ~idx ? to.substring(0, idx) : (~idx2 ? to.substring(0, idx2) : to);
                            let search = ~idx ? to.substring(idx, ~idx2 ? idx2 : undefined) : '';
                            let hash = ~idx2 ? to.substring(idx2) : location.hash;
                
                            if (!pathname) {
                                pathname = location.pathname;
                            } else if (pathname[0] !== '/' && !/^https?:\\/\\//.test(pathname)) {
                                let relativeBase = location.pathname;
                                const paths = relativeBase.split('/');
                                paths.pop();
                                let m;
                                while ((m = /^\\.\\.?\\//.exec(pathname))) {
                                    if (m[0] === '../') {
                                        paths.pop();
                                    }
                                    pathname = pathname.substring(m[0].length);
                                }
                                pathname = paths.concat(pathname).join('/');
                            }
                
                            return pathname + search + hash;
                        }
                
                        function isCurrentUrl(to, ctx) {
                            if (!to) {
                                return false;
                            }
                            const pathname = history.location.pathname;
                            const link = normalizeLink(to, {
                                ...location,
                                pathname,
                                hash: ''
                            });
                
                            if (!~link.indexOf('http') && ~link.indexOf(':')) {
                                let strict = ctx && ctx.strict;
                                return match(link, {
                                    decode: decodeURIComponent,
                                    strict: typeof strict !== 'undefined' ? strict : true
                                })(pathname);
                            }
                
                            return decodeURI(pathname) === link;
                        }
                
                        let amisInstance = amis.embed(
                            '#root',
                            app,
                            {
                                location: history.location,
                                data: {},
                                context: {
                                }
                            },
                            {
                                requestAdaptor(api) {
                                    console.log(api);
                                    return api;
                                },
                                responseAdaptor(api, payload, query, request, response) {
                                    console.log(api, payload, query, request, response);
                                    return payload;
                                },
                                updateLocation: (location, replace) => {
                                    location = normalizeLink(location);
                                    if (location === 'goBack') {
                                        return history.goBack();
                                    } else if ((!/^https?:\\/\\//.test(location) &&
                                            location === history.location.pathname + history.location.search) ||
                                        location === history.location.href) {
                                        return;
                                    } else if (/^https?:\\/\\//.test(location) || !history) {
                                        return (window.location.href = location);
                                    }
                                    history[replace ? 'replace' : 'push'](location);
                                },
                                jumpTo: (to, action) => {
                                    if (to === 'goBack') {
                                        return history.goBack();
                                    }
                                    to = normalizeLink(to);
                                    if (isCurrentUrl(to)) {
                                        return;
                                    }
                                    if (action && action.actionType === 'url') {
                                        action.blank === false ? (window.location.href = to) : window.open(to, '_blank');
                                        return;
                                    } else if (action && action.blank) {
                                        window.open(to, '_blank');
                                        return;
                                    }
                                    if (/^https?:\\/\\//.test(to)) {
                                        window.location.href = to;
                                    } else if ((!/^https?:\\/\\//.test(to) &&
                                            to === history.pathname + history.location.search) ||
                                        to === history.location.href) {
                                    } else {
                                        history.push(to);
                                    }
                                },
                                isCurrentUrl: isCurrentUrl,
                                theme: '${theme}'
                            }
                        );
                
                        history.listen(state => {
                            amisInstance.updateProps({
                                location: state.location || state
                            });
                        });
                    })();
                    ${customJs}
                </script>
                </body>
                </html>
                """;
    }

    /**
     * Get the HTML template for rendering a single schema page
     *
     * @return schema page HTML template string
     */
    private String getSchemaPageTemplate() {
        return """
                <!doctype html>
                <html lang="zh-CN">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
                    <title>${title}</title>
                
                    <link rel="stylesheet" title="default" href="${ctx}/cdn/amis/${version}/sdk.css" />
                    <link rel="stylesheet" href="${ctx}/cdn/amis/${version}/helper.css" />
                    <link rel="stylesheet" href="${ctx}/cdn/amis/${version}/iconfont.css" />
                    <link rel="stylesheet" href="${ctx}/cdn/amis/${version}/${theme}.css" />
                    <script src="${ctx}/cdn/amis/${version}/sdk.js"></script>
                    <script src="${ctx}/cdn/amis/${version}/umd/history.js"></script>
                
                    <style>
                      html, body, .app-wrapper {
                        position: relative;
                        width: 100%;
                        height: 100%;
                        margin: 0;
                        padding: 0;
                      }
                      ${customCss}
                    </style>
                  </head>
                  <body>
                    <div id="root"></div>
                
                    <script>
                      (function (global) {
                        global.AmisUtil = {
                          ready: function (callback, options) {
                            options = options || {};
                            const interval = options.interval || 200;
                            const maxAttempts = options.maxAttempts || 20;
                            let attempts = 0;
                            function tryLoad() {
                              if (typeof global.amisEmbed === "function") {
                                callback(global.amisEmbed);
                                return true;
                              }
                              if (typeof global.amisRequire === "function") {
                                try {
                                  const embed = global.amisRequire("amis/embed");
                                  callback(embed);
                                  return true;
                                } catch (e) {
                                  console.warn(
                                    "[AMIS] amisRequire is ready, but embed module is not registered",
                                  );
                                }
                              }
                              return false;
                            }
                            if (tryLoad()) return;
                            const timer = setInterval(function () {
                              attempts++;
                              if (tryLoad()) {
                                clearInterval(timer);
                              } else if (attempts >= maxAttempts) {
                                clearInterval(timer);
                                console.error("[AMIS] AMIS SDK not ready");
                              }
                            }, interval);
                          },
                        };
                      })(window);
                
                      const schemaJson = ${schemaJson};
                
                      AmisUtil.ready((amis) => {
                        amis.embed("#root", schemaJson);
                      });
                
                      ${customJs}
                    </script>
                  </body>
                </html>
                """;
    }

    /**
     * Process template with properties
     *
     * @param template   the template string
     * @param appConfig  app configuration map
     * @param properties configuration properties
     * @return processed template
     */
    private String processTemplate(String template, Map<String, Object> appConfig, AmisUiProperties properties) {
        try {
            String appJson = objectMapper.writeValueAsString(appConfig);
            log.debug("Generated app.json: {}", appJson);

            return template
                    .replace("${title}", properties.getApp().getTitle())
                    .replace("${version}", properties.getVersion())
                    .replace("${appJson}", appJson)
                    .replace("${apiHost}", StringUtils.hasText(properties.getApiHost()) ? properties.getApiHost() : "")
                    .replace("${ctx}", StringUtils.hasText(properties.getCtx()) ? properties.getCtx() : environment.getProperty("server.servlet.context-path", ""))
                    .replace("${theme}", properties.getApp().getTheme())
                    .replace("${customCss}", StringUtils.hasText(properties.getCustomCss()) ? properties.getCustomCss() : "")
                    .replace("${customJs}", StringUtils.hasText(properties.getCustomJs()) ? properties.getCustomJs() : "");
        } catch (Exception e) {
            throw new RuntimeException("Failed to process Amis UI template", e);
        }
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
