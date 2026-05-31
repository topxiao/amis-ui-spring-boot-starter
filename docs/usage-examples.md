# 使用示例

本文档覆盖常见使用场景，所有示例基于当前 API，可直接复制使用。

---

## 1. 最小配置

添加依赖后，零配置即可启动。默认访问 `/amis` 路径。

```yaml
# application.yml — 最简配置，所有属性都有默认值
amis:
  app:
    brand-name: "My App"
```

启动后访问 `http://localhost:8080/amis` 看到 Amis 空白框架（无页面菜单）。

---

## 2. 带菜单的 App 模式

这是最常见用法——Amis 自动渲染侧边栏 + 多页导航。通过 `pages` 配置菜单结构。

> **重要**：amis app 组件要求顶层 pages 每一项都必须包含 `children`（即菜单分组）。不含 `children` 的简单页面不会被渲染到侧边栏。

```yaml
amis:
  path: /admin
  version: "6.12.0"
  schema-prefix: "classpath:amis/"
  cache-enabled: true
  app:
    brand-name: "管理后台"
    logo: "/img/logo.png"
    theme: "antd"
    header:
      - type: "dropdown-button"
        label: "帮助"
        buttons:
          - type: "button"
            label: "文档"
            url: "https://aisuda.bce.baidu.com/amis"
          - type: "button"
            label: "关于"
            url: "/about"
  pages:
    - label: "首页"
      children:
        - label: "主页"
          url: "/"
          redirect: "/dashboard"
          icon: "fa fa-home"
        - label: "仪表板"
          url: "/dashboard"
          schema-api: "get:/api/pages/dashboard"
          icon: "fa fa-dashboard"
    - label: "数据管理"
      icon: "fa fa-database"
      children:
        - label: "用户列表"
          url: "/users"
          schema-api: "get:/api/pages/users"
          icon: "fa fa-users"
        - label: "订单列表"
          url: "/orders"
          schema-api: "get:/api/pages/orders"
          icon: "fa fa-shopping-cart"
    - label: "外部系统"
      icon: "fa fa-external-link"
      children:
        - label: "GitHub"
          link: "https://github.com"
          icon: "fa fa-github"
```

启动后访问 `http://localhost:8080/admin`，Amis 渲染完整的管理后台框架。

> **schema-api 说明**：Amis 前端通过 `schema-api` 指定的 URL 动态拉取页面 Schema JSON。你需要自己实现对应的 API 端点返回 Schema，格式为 `{status: 0, msg: "", data: {schema}}`。

---

## 3. Schema 模式 — Classpath JSON 加载

`ClasspathAmisSchemaProvider` 从 `classpath:{schema-prefix}{name}.json` 自动加载 Schema 文件。

```java
@Controller
public class ViewController {

    // 返回 "amis:users" → ClasspathAmisSchemaProvider 加载 classpath:amis/users.json
    @GetMapping("/users")
    public ModelAndView users() {
        return new ModelAndView("amis:users", Map.of("title", "用户管理"));
    }

    // 支持子目录：amis:system/roles → 加载 classpath:amis/system/roles.json
    @GetMapping("/system/roles")
    public ModelAndView roles() {
        return new ModelAndView("amis:system/roles", Map.of("title", "角色管理"));
    }
}
```

对应的文件结构：

```
src/main/resources/
└── amis/
    ├── users.json
    └── system/
        └── roles.json
```

> 如果 `cache-enabled: true`（默认），加载过的 JSON 会缓存在内存中。

---

## 4. Schema 模式 — 自定义 AmisSchemaProvider

当 classpath 中没有对应 JSON 文件时，自定义 Provider 可以兜底提供动态生成的 Schema。

```java
@Component
@Order(100)  // 介于 Classpath(最高) 和 Properties(最低) 之间
public class RuntimeInfoSchemaProvider implements AmisSchemaProvider {

    @Override
    public String resolveSchema(String name) {
        if (!"runtime-info".equals(name)) {
            return null;
        }

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();

        return """
            {
              "type": "page",
              "title": "运行时信息",
              "body": {
                "type": "panel",
                "title": "JVM 信息",
                "body": [
                  { "type": "static", "label": "运行时间", "value": "%d ms" }
                ]
              }
            }
            """.formatted(uptimeMs);
    }
}
```

Controller 使用：

```java
@GetMapping("/runtime-info")
public ModelAndView runtimeInfo() {
    return new ModelAndView("amis:runtime-info", Map.of("title", "运行时信息"));
}
```

### Provider Chain 优先级

```
ClasspathAmisSchemaProvider (HIGHEST_PRECEDENCE)
  → 有 classpath:amis/{name}.json 则立即返回
  → 没有则返回 null，交给下一个
自定义 Provider (@Order(100))
  → 处理特定名称
  → 不匹配则返回 null
PropertiesAppSchemaProvider (LOWEST_PRECEDENCE)
  → 仅处理 name="app"
  → 从 AmisProperties 构建 app JSON
```

> 如果 classpath 中存在 `app.json`，它会覆盖 `PropertiesAppSchemaProvider`。

---

## 5. 自定义访问路径

```yaml
amis:
  path: /dashboard    # 默认 /amis，可改为任意路径
```

也可以禁用自动映射：

```yaml
amis:
  enabled: false      # 完全禁用 starter
```

---

## 6. Controller 中编程式渲染

### 6.1 Schema 模式渲染

```java
@RestController
public class PageController {

    private final AmisViewService viewService;

    public PageController(AmisViewService viewService) {
        this.viewService = viewService;
    }

    @GetMapping("/report")
    public ResponseEntity<String> report() {
        String schema = """
            {
              "type": "page",
              "title": "报表中心",
              "body": {
                "type": "crud",
                "api": "/api/report/list",
                "columns": [
                  { "name": "id", "label": "ID" },
                  { "name": "name", "label": "名称" },
                  { "name": "amount", "label": "金额" }
                ]
              }
            }
            """;

        String html = viewService.renderHtml(schema, "报表中心");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
```

### 6.2 带 customCss / customJs 渲染

```java
@GetMapping("/styled-report")
public ResponseEntity<String> styledReport() {
    String schema = """
        { "type": "page", "body": "styled content" }
        """;
    String customCss = ".my-banner { background: #667eea; color: white; padding: 20px; }";
    String customJs = "console.log('page loaded at', new Date().toISOString());";

    // renderHtml(schemaJson, title, customCss, customJs)
    String html = viewService.renderHtml(schema, "样式报表", customCss, customJs);
    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
}
```

> customCss 会注入到 `<style>` 块，customJs 注入到 `<script>` 块。

### 6.3 App 模式渲染

```java
// 使用 YAML 中配置的 pages 渲染完整 App
@GetMapping("/custom-app")
public ResponseEntity<String> customApp() {
    String html = viewService.renderHtml();
    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
}

// App 模式带自定义数据
@GetMapping("/app-with-data")
public ResponseEntity<String> appWithData() {
    Map<String, Object> data = new HashMap<>();
    data.put("extraInfo", "动态注入数据");

    String html = viewService.renderHtml(data);
    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
}
```

### AmisViewService 方法一览

| 方法 | 说明 |
|------|------|
| `renderHtml()` | App 模式，无自定义数据 |
| `renderHtml(Map<String, Object>)` | App 模式，带自定义数据 |
| `renderHtml(String schemaJson)` | Schema 模式，默认标题 |
| `renderHtml(String schemaJson, String title)` | Schema 模式，指定标题 |
| `renderHtml(String, String, String customCss, String customJs)` | Schema 模式，完整参数 |

---

## 7. Spring MVC 视图名渲染

Starter 注册了 `AmisViewResolver`，解析 `amis:` 前缀视图名。所有名称统一通过 `AmisSchemaProvider` chain 解析。

```java
@Controller
public class ViewController {

    // amis:app → PropertiesAppSchemaProvider 从 yml 配置构建 app JSON
    // 使用 app 模板（hash 路由 + 侧边栏）
    @GetMapping("/my-admin")
    public String admin() {
        return "amis:app";
    }

    // amis:users → ClasspathAmisSchemaProvider 加载 classpath:amis/users.json
    // 使用 schema 模板（单页面）
    @GetMapping("/users")
    public ModelAndView users() {
        return new ModelAndView("amis:users", Map.of("title", "用户管理"));
    }

    // amis:runtime-info → 自定义 Provider 生成动态 Schema
    @GetMapping("/runtime-info")
    public ModelAndView runtimeInfo() {
        return new ModelAndView("amis:runtime-info", Map.of("title", "运行时信息"));
    }
}
```

### customCss / customJs 通过 Model 注入

```java
@GetMapping("/styled")
public ModelAndView styled() {
    Map<String, Object> model = new HashMap<>();
    model.put("title", "自定义样式");
    model.put("customCss", ".my-banner { background: #667eea; color: white; }");
    model.put("customJs", "console.log('injected');");

    // schema 通过 model 传入（但优先使用 provider chain 解析）
    model.put("schema", "{\"type\":\"page\",\"body\":\"styled\"}");
    return new ModelAndView("amis:styled", model);
}
```

---

## 8. 扩展：动态修改配置属性

```java
@Component
@Order(1)
public class EnvPropertiesCustomizer implements AmisPropertiesCustomizer {

    @Override
    public AmisProperties customize(AmisProperties properties) {
        // ⚠️ 注意：properties 是 Spring 管理的单例对象，不要修改它的字段！
        // 错误做法：properties.getApp().setTitle(...) — 修改会累积
        // 推荐做法：只读取不修改，或创建新的 AmisProperties 对象返回

        String env = System.getProperty("spring.profiles.active", "dev");
        AmisProperties copy = new AmisProperties();
        copy.setApp(new AmisProperties.App());
        copy.getApp().setBrandName(
            properties.getApp().getBrandName() + " [" + env.toUpperCase() + "]"
        );
        return copy;
    }
}
```

多个 `AmisPropertiesCustomizer` 按 `@Order` 顺序依次执行，前一个的输出是后一个的输入。

---

## 9. 扩展：动态添加页面

```java
@Component
public class DynamicPageCustomizer implements AmisPageCustomizer {

    @Override
    public List<AmisProperties.Page> customizePages(
            List<AmisProperties.Page> pages, AmisProperties properties) {
        List<AmisProperties.Page> result = new ArrayList<>(pages);

        // 使用 AmisPages 工厂方法追加菜单分组
        if (hasAdminRole()) {
            result.add(AmisPages.group("管理面板", "fa fa-shield",
                    AmisPages.children(
                            AmisPages.page("系统配置", "/admin/config",
                                    "/api/schema/config", "fa fa-cog")
                    )));
        }

        return result;
    }

    private boolean hasAdminRole() {
        return true;
    }
}
```

---

## 10. 扩展：渲染拦截器

```java
@Component
public class GaTrackerInterceptor implements AmisRenderInterceptor {

    @Override
    public void beforeRender(AmisRenderContext context) {
        // context.getTemplateType() → "app" 或 "schema"
        // context.getData() → 渲染数据 Map
        context.set("trackingId", "G-XXXXXXXXXX");
    }

    @Override
    public String afterRender(AmisRenderContext context, String html) {
        String trackingId = (String) context.get("trackingId");
        if (trackingId != null) {
            String script = """
                <script async src="https://www.googletagmanager.com/gtag/js?id=%s"></script>
                <script>
                  window.dataLayer = window.dataLayer || [];
                  function gtag(){dataLayer.push(arguments);}
                  gtag('js', new Date());
                  gtag('config', '%s');
                </script>
                """.formatted(trackingId, trackingId);
            return html.replace("<head>", "<head>" + script);
        }
        return html;
    }
}
```

> 拦截器抛异常时会被跳过并记录 WARN 日志，不影响渲染。

---

## 11. 使用 AmisPages 工具类

`AmisPages` 提供静态方法快速构建 `Page` 对象：

```java
import com.github.topxiao.amisui.AmisPages;
import com.github.topxiao.amisui.AmisProperties;

// 简单页面
AmisProperties.Page userPage = AmisPages.page(
    "用户管理",       // label
    "/users",        // url
    "get:/api/pages/users",  // schemaApi
    "fa fa-users"    // icon
);

// 带子页面的页面
AmisProperties.Page parentPage = AmisPages.page(
    "父页面", "/parent", "/api/pages/parent", "fa fa-sitemap",
    AmisPages.children(
        AmisPages.page("子页面A", "/parent/a", "/api/pages/a", "fa fa-file"),
        AmisPages.page("子页面B", "/parent/b", "/api/pages/b", "fa fa-file")
    )
);

// 菜单分组（无 url，仅作为子菜单容器）
AmisProperties.Page systemGroup = AmisPages.group(
    "系统管理",       // label
    "fa fa-cog",     // icon
    AmisPages.children(
        AmisPages.page("角色管理", "/roles", "get:/api/pages/roles", "fa fa-key"),
        AmisPages.page("日志查看", "/logs", "get:/api/pages/logs", "fa fa-list")
    )
);

// 辅助方法
AmisPages.children(page1, page2, page3);  // 打包为数组，null 安全
AmisPages.emptyChildren();                  // 返回空数组

List<AmisProperties.Page> pages = List.of(userPage, parentPage, systemGroup);
```

---

## 12. 带上下文路径部署

当应用部署在非根路径时（如 `https://example.com/myapp`），配置 `ctx`：

```yaml
server:
  servlet:
    context-path: /myapp

amis:
  path: /admin
  # ctx 不需要手动配，starter 自动从 server.servlet.context-path 读取
  # 如果需要覆盖：
  # ctx: /myapp
```

---

## 配置速查

```yaml
amis:
  enabled: true             # 是否启用（默认 true）
  path: /amis               # 自动映射路径（默认 /amis）
  version: "6.12.0"         # Amis SDK 版本（默认 6.12.0）
  ctx: ""                   # 上下文路径（默认自动读取 server.servlet.context-path）
  schema-prefix: "classpath:amis/"  # JSON Schema 文件前缀（默认 classpath:amis/）
  cache-enabled: true       # 是否启用 Schema 文件内存缓存（默认 true）
  app:
    brand-name: "Admin"     # 品牌名（默认 Admin）
    logo: "img/logo.png"    # Logo（默认 img/logo.png）
    title: "Admin"          # 标题（默认 Admin）
    theme: "ang"            # 主题（默认 ang）
    header: []              # 顶部导航配置（可选）
  pages: []                 # 菜单页面列表（可选）
```

---

## Schema API 响应格式

`schema-api` 指向的 API 端点必须返回以下格式：

```json
{
  "status": 0,
  "msg": "",
  "data": {
    "type": "page",
    "title": "页面标题",
    "body": "页面内容"
  }
}
```

示例 Controller：

```java
@GetMapping("/api/schema/{name}")
public Map<String, Object> schemaApi(@PathVariable String name) {
    Map<String, Object> schema = switch (name) {
        case "users" -> Map.of(
                "type", "page",
                "title", "用户管理",
                "body", Map.of("type", "crud", "api", "/api/users")
        );
        default -> Map.of("type", "page", "title", "未找到", "body", "无此页面");
    };
    return Map.of("status", 0, "msg", "", "data", schema);
}
```

---

## Bean 替换

Starter 的核心 bean 均使用 `@ConditionalOnMissingBean` 注册，可在项目中定义同类型 bean 替换默认实现：

```java
@Configuration
public class MyAmisConfig {

    // 替换默认的 AmisViewService
    @Bean
    public AmisViewService amisViewService(AmisProperties properties,
                                           Environment environment,
                                           ObjectMapper objectMapper) {
        return new MyCustomAmisViewService(properties, environment, objectMapper);
    }

    // 替换默认的 ClasspathAmisSchemaProvider
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ClasspathAmisSchemaProvider classpathAmisSchemaProvider(
            AmisProperties properties, ResourceLoader resourceLoader) {
        return new MyCustomSchemaProvider(resourceLoader,
                properties.getSchemaPrefix(), properties.isCacheEnabled());
    }
}
```

| 可替换的 Bean | 类型 |
|---------------|------|
| `AmisViewService` | 核心渲染服务 |
| `ClasspathAmisSchemaProvider` | Classpath Schema 解析器 |
| `PropertiesAppSchemaProvider` | App 配置 Schema 解析器 |
