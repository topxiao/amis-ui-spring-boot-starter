package com.github.topxiao.amisui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.topxiao.amisui.ext.AmisUiRenderInterceptor;
import com.github.topxiao.amisui.ext.AmisUiRenderInterceptor.RenderContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Spring MVC {@link View} that renders amis HTML pages.
 * <p>
 * Supports two rendering modes:
 * <ul>
 *   <li><b>Schema mode</b> (appMode=false): renders a single amis schema page,
 *       expects a {@code schema} attribute in the model.</li>
 *   <li><b>App mode</b> (appMode=true): renders a full amis app with multi-page
 *       navigation, driven by the configured pages in {@link AmisUiProperties}.</li>
 * </ul>
 */
public class AmisView implements View {

    private static final String DEFAULT_TITLE = "AMIS Page";
    private static final String DEFAULT_THEME = "ang";

    private final AmisUiService service;
    private final boolean appMode;

    /**
     * Create a new AmisView.
     *
     * @param service the AmisUiService providing context building methods
     * @param appMode true for app mode (multi-page), false for schema mode (single page)
     */
    public AmisView(AmisUiService service, boolean appMode) {
        this.service = service;
        this.appMode = appMode;
    }

    @Override
    public String getContentType() {
        return "text/html;charset=UTF-8";
    }

    /**
     * Render the amis page and write to the HTTP response.
     */
    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Convert to mutable Map<String, Object> for renderToString
        Map<String, Object> mutableModel = new HashMap<>();
        if (model != null) {
            mutableModel.putAll(model);
        }

        String html = renderToString(mutableModel);
        response.setContentType(getContentType());
        response.getWriter().write(html);
    }

    /**
     * Render the amis page to an HTML string.
     * Package-private for use by {@link AmisUiService} backward compatibility.
     *
     * @param model the model map containing rendering attributes
     * @return the rendered HTML string
     */
    String renderToString(Map<String, Object> model) {
        if (appMode) {
            return renderAppMode(model);
        } else {
            return renderSchemaMode(model);
        }
    }

    /**
     * Package-private accessor for testing.
     */
    boolean isAppMode() {
        return appMode;
    }

    // -------------------------------------------------------------------------
    // Schema mode rendering
    // -------------------------------------------------------------------------

    /**
     * Render in schema (single-page) mode.
     * <p>
     * Expects {@code schema} in the model (String JSON or Map).
     * Validates/serializes schema via {@link com.fasterxml.jackson.databind.ObjectMapper}
     * to prevent XSS injection.
     */
    private String renderSchemaMode(Map<String, Object> model) {
        // Schema is required for schema mode
        Object schema = model.get("schema");
        if (schema == null) {
            throw new IllegalArgumentException("schema attribute is required for amis:page view");
        }

        String schemaJson = resolveSchemaJson(schema);

        // Apply properties customizers
        AmisUiProperties customizedProperties = service.applyPropertiesCustomizers();

        // Resolve template variables from model overrides or defaults
        String title = getStringOrDefault(model, "title", DEFAULT_TITLE);
        String customCss = getStringOrDefault(model, "customCss", "");
        String customJs = getStringOrDefault(model, "customJs", "");
        String ctx = resolveCtx(customizedProperties);
        String theme = resolveTheme(customizedProperties);
        String version = customizedProperties.getVersion();

        // Build HTML from template
        String html = getSchemaPageTemplate()
                .replace("${title}", title)
                .replace("${version}", version)
                .replace("${schemaJson}", schemaJson)
                .replace("${ctx}", ctx)
                .replace("${theme}", theme)
                .replace("${customCss}", customCss)
                .replace("${customJs}", customJs);

        // Apply after-render interceptors
        RenderContext context = new RenderContext(model, "schema");
        html = service.invokeAfterRenderInterceptors(context, html);

        return html;
    }

    // -------------------------------------------------------------------------
    // App mode rendering
    // -------------------------------------------------------------------------

    /**
     * Render in app (multi-page) mode.
     * <p>
     * Uses pages configured in {@link AmisUiProperties}, invokes
     * before-render interceptors before building context, and
     * after-render interceptors after HTML generation.
     */
    private String renderAppMode(Map<String, Object> model) {
        // Apply properties customizers
        AmisUiProperties customizedProperties = service.applyPropertiesCustomizers();

        // Before-render interceptors
        RenderContext context = new RenderContext(model, "app");
        service.invokeBeforeRenderInterceptors(context);

        // Build app configuration
        Map<String, Object> appConfig = service.buildAppConfig(customizedProperties);

        // Resolve template variables
        String ctx = resolveCtx(customizedProperties);
        String theme = resolveTheme(customizedProperties);
        String version = customizedProperties.getVersion();
        String apiHost = Optional.ofNullable(customizedProperties.getApiHost())
                .filter(StringUtils::hasText)
                .orElse("");
        String customCss = Optional.ofNullable(customizedProperties.getCustomCss())
                .filter(StringUtils::hasText)
                .orElse("");
        String customJs = Optional.ofNullable(customizedProperties.getCustomJs())
                .filter(StringUtils::hasText)
                .orElse("");
        String title = customizedProperties.getApp().getTitle();

        // Serialize app config to JSON
        String appJson;
        try {
            appJson = service.getObjectMapper().writeValueAsString(appConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize app configuration", e);
        }

        // Build HTML from template
        String html = getHtmlTemplate()
                .replace("${title}", title)
                .replace("${version}", version)
                .replace("${apiHost}", apiHost)
                .replace("${appJson}", appJson)
                .replace("${ctx}", ctx)
                .replace("${theme}", theme)
                .replace("${customCss}", customCss)
                .replace("${customJs}", customJs);

        // After-render interceptors
        html = service.invokeAfterRenderInterceptors(context, html);

        return html;
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Resolve context path: from customizedProperties, or fall back to environment.
     */
    private String resolveCtx(AmisUiProperties customizedProperties) {
        return Optional.ofNullable(customizedProperties.getCtx())
                .filter(StringUtils::hasText)
                .orElseGet(() -> {
                    Environment env = service.getEnvironment();
                    return env != null ? env.getProperty("server.servlet.context-path", "") : "";
                });
    }

    /**
     * Resolve theme: from customizedProperties, or fall back to default "ang".
     */
    private String resolveTheme(AmisUiProperties customizedProperties) {
        return Optional.ofNullable(customizedProperties.getApp().getTheme())
                .filter(StringUtils::hasText)
                .orElse(DEFAULT_THEME);
    }

    /**
     * Get a string value from the model, or return the default.
     */
    private String getStringOrDefault(Map<String, Object> model, String key, String defaultValue) {
        Object value = model.get(key);
        if (value instanceof String s && StringUtils.hasText(s)) {
            return s;
        }
        return defaultValue;
    }

    /**
     * Validate and serialize a schema object to JSON string.
     * <p>
     * Accepts String (raw JSON) or Map (already parsed).
     * Both are validated by round-tripping through ObjectMapper to prevent XSS.
     *
     * @param schema the schema object (String or Map)
     * @return validated JSON string
     * @throws IllegalArgumentException if schema is not valid JSON
     */
    private String resolveSchemaJson(Object schema) {
        try {
            // Parse: if String, read as tree; if Map, use directly
            Object parsed;
            if (schema instanceof String s) {
                parsed = service.getObjectMapper().readValue(s, Object.class);
            } else {
                parsed = schema; // Map or other object, let ObjectMapper serialize it
            }
            // Re-serialize to ensure valid JSON and prevent injection
            return service.getObjectMapper().writeValueAsString(parsed);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid schema: must be valid JSON", e);
        }
    }

    // -------------------------------------------------------------------------
    // Templates (copied verbatim from AmisUiService)
    // -------------------------------------------------------------------------

    /**
     * HTML template for app mode (multi-page with navigation).
     * Same as {@code AmisUiService.getHtmlTemplate()}.
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
                                    return api;
                                },
                                responseAdaptor(api, payload, query, request, response) {
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
     * HTML template for schema mode (single-page).
     * Same as {@code AmisUiService.getSchemaPageTemplate()}.
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
}
