package com.github.topxiao.amisui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.topxiao.amisui.ext.AmisRenderContext;
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
 * Amis HTML 页面渲染 View。
 * <p>
 * 支持两种渲染模式：
 * <ul>
 *   <li><b>Schema 模式</b> (appMode=false)：渲染单页面 amis 视图，
 *       从 model 的 {@code schema} 属性或 preloadedSchema 获取 JSON。</li>
 *   <li><b>App 模式</b> (appMode=true)：渲染多页面 amis 框架，
 *       包含 hash 路由、导航栏，从 AmisProperties 构建 app 配置。</li>
 * </ul>
 * 渲染模式由 {@link AmisViewResolver} 根据视图名称决定："app" 使用 App 模式，其余使用 Schema 模式。
 */
public class AmisView implements View {

    private static final String DEFAULT_TITLE = "AMIS Page";
    private static final String DEFAULT_THEME = "ang";

    // @formatter:off
    private static final String APP_TEMPLATE = """
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

    private static final String SCHEMA_TEMPLATE = """
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
    // @formatter:on

    private final AmisViewService service;
    /**
     * true=多页面框架模板(APP_TEMPLATE)，false=单页面模板(SCHEMA_TEMPLATE)
     */
    private final boolean appMode;
    /**
     * 预加载的 Schema JSON，由 AmisViewResolver 通过 provider chain 预先解析好
     */
    private final String preloadedSchema;

    /**
     * 自行构建 Schema 的构造函数（AmisViewService.renderHtml 使用）
     */
    public AmisView(AmisViewService service, boolean appMode) {
        this(service, appMode, null);
    }

    /**
     * 预加载 Schema 的构造函数（AmisViewResolver 使用）
     */
    public AmisView(AmisViewService service, boolean appMode, String preloadedSchema) {
        this.service = service;
        this.appMode = appMode;
        this.preloadedSchema = preloadedSchema;
    }

    @Override
    public String getContentType() {
        return "text/html;charset=UTF-8";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> mutableModel = new HashMap<>();
        if (model != null) {
            mutableModel.putAll(model);
        }

        String html = renderToString(mutableModel);
        response.setContentType(getContentType());
        response.getWriter().write(html);
    }

    String renderToString(Map<String, Object> model) {
        return appMode ? renderAppMode(model) : renderSchemaMode(model);
    }

    boolean isAppMode() {
        return appMode;
    }

    // -------------------------------------------------------------------------
    // Schema mode
    // -------------------------------------------------------------------------

    private String renderSchemaMode(Map<String, Object> model) {
        Object schema = preloadedSchema != null ? preloadedSchema : model.get("schema");
        if (schema == null) {
            throw new IllegalArgumentException("schema attribute is required for amis:page view");
        }

        String schemaJson = resolveSchemaJson(schema);
        AmisProperties props = service.applyPropertiesCustomizers();

        String html = SCHEMA_TEMPLATE
                .replace("${title}", getStringOrDefault(model, "title", DEFAULT_TITLE))
                .replace("${version}", props.getVersion())
                .replace("${schemaJson}", schemaJson)
                .replace("${ctx}", resolveCtx(props))
                .replace("${theme}", resolveTheme(props))
                .replace("${customCss}", getStringOrDefault(model, "customCss", ""))
                .replace("${customJs}", getStringOrDefault(model, "customJs", ""));

        AmisRenderContext context = new AmisRenderContext(model, "schema");
        return service.invokeAfterRenderInterceptors(context, html);
    }

    // -------------------------------------------------------------------------
    // App mode
    // -------------------------------------------------------------------------

    private String renderAppMode(Map<String, Object> model) {
        AmisProperties props = service.applyPropertiesCustomizers();

        AmisRenderContext context = new AmisRenderContext(model, "app");
        service.invokeBeforeRenderInterceptors(context);

        String appJson;
        if (preloadedSchema != null) {
            appJson = preloadedSchema;
        } else {
            try {
                appJson = service.getObjectMapper().writeValueAsString(service.buildAppConfig(props));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize app configuration", e);
            }
        }

        String html = APP_TEMPLATE
                .replace("${title}", props.getApp().getTitle())
                .replace("${version}", props.getVersion())
                .replace("${appJson}", appJson)
                .replace("${ctx}", resolveCtx(props))
                .replace("${theme}", resolveTheme(props))
                .replace("${customCss}", getStringOrDefault(model, "customCss", ""))
                .replace("${customJs}", getStringOrDefault(model, "customJs", ""));

        return service.invokeAfterRenderInterceptors(context, html);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String resolveCtx(AmisProperties props) {
        return Optional.ofNullable(props.getCtx())
                .filter(StringUtils::hasText)
                .orElseGet(() -> {
                    Environment env = service.getEnvironment();
                    return env != null ? env.getProperty("server.servlet.context-path", "") : "";
                });
    }

    private String resolveTheme(AmisProperties props) {
        return Optional.ofNullable(props.getApp().getTheme())
                .filter(StringUtils::hasText)
                .orElse(DEFAULT_THEME);
    }

    private String getStringOrDefault(Map<String, Object> model, String key, String defaultValue) {
        Object value = model.get(key);
        if (value instanceof String s && StringUtils.hasText(s)) {
            return s;
        }
        return defaultValue;
    }

    private String resolveSchemaJson(Object schema) {
        try {
            Object parsed = schema instanceof String s
                    ? service.getObjectMapper().readValue(s, Object.class)
                    : schema;
            return service.getObjectMapper().writeValueAsString(parsed);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid schema: must be valid JSON", e);
        }
    }
}
