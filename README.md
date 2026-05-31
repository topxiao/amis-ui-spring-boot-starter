# Amis UI Spring Boot 3 Starter

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.topxiao/amis-ui-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.topxiao/amis-ui-spring-boot-starter)

Spring Boot 3 Starter，用于快速集成 [Amis](https://aisuda.bce.baidu.com/amis/zh-CN/docs/index) 低代码前端框架。

## 功能特性

- 自动集成 Amis SDK 静态资源
- Spring MVC 原生视图解析（ViewControllerMapping + ViewResolver）
- `@ConfigurationProperties` 类型安全配置
- Schema Provider 链式解析（classpath JSON、配置属性、自定义）
- 可扩展的属性自定义器、页面自定义器、渲染拦截器

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.github.topxiao</groupId>
    <artifactId>amis-ui-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 配置应用

在 `application.yml` 中配置：

```yaml
amis:
  path: /amis
  version: "6.12.0"
  schema-prefix: "classpath:amis/"
  cache-enabled: true
  app:
    brand-name: "My Admin"
    logo: "/img/logo.png"
    title: "管理系统"
    theme: "ang"
  pages:
    - label: "首页"
      children:
        - label: "主页"
          url: "/"
          redirect: "/dashboard"
          icon: "fa fa-home"
        - label: "仪表板"
          url: "/dashboard"
          schema-api: "get:/api/schema/dashboard"
          icon: "fa fa-dashboard"
    - label: "系统管理"
      icon: "fa fa-cog"
      children:
        - label: "用户管理"
          url: "/users"
          schema-api: "get:/api/schema/users"
          icon: "fa fa-user"
        - label: "角色管理"
          url: "/roles"
          schema-api: "get:/api/schema/roles"
          icon: "fa fa-shield"
```

> **pages 配置要求**：amis app 组件要求顶层 pages 的每一项都必须包含 `children`（即菜单分组）。不含 `children` 的简单页面不会被渲染到侧边栏。使用 `link` 字段的外部链接不受此限制。

### 3. 创建 Schema 文件

在 `src/main/resources/amis/` 下放置 JSON Schema 文件，starter 会自动加载：

```json
// src/main/resources/amis/users.json
{
  "type": "page",
  "title": "用户管理",
  "body": {
    "type": "crud",
    "api": "/api/users",
    "columns": [
      { "name": "id", "label": "ID" },
      { "name": "name", "label": "姓名" },
      { "name": "email", "label": "邮箱" }
    ]
  }
}
```

### 4. 启动应用

启动后访问 `http://localhost:8080/amis` 即可看到 Amis UI 界面。

## 配置属性

所有配置前缀为 `amis`。

### 顶层属性

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `amis.enabled` | Boolean | `true` | 是否启用 Amis UI |
| `amis.path` | String | `"/amis"` | Amis 页面映射路径 |
| `amis.version` | String | `"6.12.0"` | Amis SDK 版本 |
| `amis.ctx` | String | `""` | 应用上下文路径（默认自动读取 `server.servlet.context-path`） |
| `amis.schema-prefix` | String | `"classpath:amis/"` | ClasspathAmisSchemaProvider 的 JSON 文件前缀 |
| `amis.cache-enabled` | Boolean | `true` | 是否启用 Schema 文件内存缓存 |

### App 属性

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `amis.app.brand-name` | String | `"Admin"` | 应用品牌名称 |
| `amis.app.logo` | String | `"img/logo.png"` | 应用 Logo 路径 |
| `amis.app.title` | String | `"Admin"` | 应用标题 |
| `amis.app.theme` | String | `"ang"` | 应用主题 |
| `amis.app.header` | List\<Map\> | `null` | 顶部导航配置 |

### 页面属性

页面配置支持以下字段：

- `label`: 页面显示名称
- `url`: 页面 URL 路径
- `redirect`: 重定向 URL
- `schema-api`: Amis Schema API 地址
- `link`: 外部链接（新标签页打开）
- `icon`: 页面图标
- `children`: 子页面（用于创建多级菜单）

## Schema Provider 链

Starter 通过 `AmisSchemaProvider` 链解析视图名对应的 Schema JSON。`AmisViewResolver` 按顺序调用所有 provider，第一个返回非 `null` 的结果生效。

### 内置 Provider

| Provider | 优先级 | 行为 |
|----------|--------|------|
| `ClasspathAmisSchemaProvider` | `@Order(HIGHEST_PRECEDENCE)` | 从 `classpath:{schema-prefix}{name}.json` 加载文件 |
| `PropertiesAppSchemaProvider` | `@Order(LOWEST_PRECEDENCE)` | 从 `AmisProperties` 构建 app 配置，仅处理 name="app" |

### 解析流程

```
Controller 返回 "amis:users"
  → AmisViewResolver 剥离前缀，得到 name="users"
  → 遍历 provider chain：
    1. ClasspathAmisSchemaProvider.resolveSchema("users")
       → 加载 classpath:amis/users.json → 返回 JSON 字符串 ✓
    2. PropertiesAppSchemaProvider 不处理非 "app" 名称 → 跳过
  → 创建 AmisView(schema 模板) 渲染
```

```
Controller 返回 "amis:app"
  → 遍历 provider chain：
    1. ClasspathAmisSchemaProvider 检查 classpath:amis/app.json
       → 不存在则返回 null
    2. PropertiesAppSchemaProvider.resolveSchema("app")
       → 从 AmisProperties 构建 app JSON ✓
  → 创建 AmisView(app 模板) 渲染
```

> 如果 classpath 中存在 `app.json`，它会覆盖 `PropertiesAppSchemaProvider`，因为 `ClasspathAmisSchemaProvider` 优先级更高。

### 自定义 SchemaProvider

注册 `@Component` + `@Order` 即可插入自定义解析逻辑：

```java
@Component
@Order(100)  // 介于 Classpath(最高) 和 Properties(最低) 之间
public class RuntimeInfoSchemaProvider implements AmisSchemaProvider {

    @Override
    public String resolveSchema(String name) {
        if (!"runtime-info".equals(name)) {
            return null;  // 不处理其他名称
        }

        return """
            {
              "type": "page",
              "title": "运行时信息",
              "body": {
                "type": "panel",
                "title": "JVM 信息",
                "body": "动态生成的内容"
              }
            }
            """;
    }
}
```

## 视图名渲染

Starter 注册了 `AmisViewResolver`，解析 `amis:` 前缀视图名：

```java
@Controller
public class ViewController {

    // amis:app → 通过 provider chain 解析 "app"，使用 app 模板
    @GetMapping("/admin")
    public String admin() {
        return "amis:app";
    }

    // amis:users → 通过 provider chain 解析 "users"，使用 schema 模板
    // ClasspathAmisSchemaProvider 自动加载 classpath:amis/users.json
    @GetMapping("/users")
    public ModelAndView users() {
        return new ModelAndView("amis:users", Map.of("title", "用户管理"));
    }

    // amis:xxx → 如果 classpath 中没有 xxx.json 且无自定义 provider 处理，
    // resolver 返回 null，交给下一个 ViewResolver
}
```

### customCss / customJs

通过 ModelAndView 的 model 传入自定义 CSS/JS，注入到页面 `<style>` 和 `<script>` 块：

```java
@GetMapping("/styled")
public ModelAndView styled() {
    Map<String, Object> model = new HashMap<>();
    model.put("title", "自定义样式");
    model.put("customCss", ".my-banner { background: #667eea; color: white; padding: 20px; }");
    model.put("customJs", "console.log('injected at', new Date().toISOString());");
    model.put("schema", "{\"type\":\"page\",\"body\":\"styled page\"}");
    return new ModelAndView("amis:styled", model);
}
```

## 扩展机制

Starter 通过 Spring `List<T>` 构造器注入收集扩展，注册 `@Component` 即可自动生效。

### 扩展点

| 接口 | 方法 | 使用场景 |
|------|------|----------|
| `AmisSchemaProvider` | `resolveSchema(String name)` | 自定义 Schema 解析逻辑 |
| `AmisPropertiesCustomizer` | `customize(AmisProperties)` | 渲染前动态修改配置属性 |
| `AmisPageCustomizer` | `customizePages(List<Page>, AmisProperties)` | 动态添加/修改页面菜单 |
| `AmisRenderInterceptor` | `beforeRender(ctx)` / `afterRender(ctx, html)` | 拦截渲染过程 |

### 1. 自定义 SchemaProvider

见上方 [自定义 SchemaProvider](#自定义-schemaprovider) 章节。

### 2. 属性配置自定义器

```java
@Component
@Order(1)
public class EnvPropertiesCustomizer implements AmisPropertiesCustomizer {

    @Override
    public AmisProperties customize(AmisProperties properties) {
        // ⚠️ 注意：properties 是 Spring 管理的单例对象，每次请求都会复用。
        // 不要修改它的字段（如 getApp().setTitle(...)），否则修改会累积。
        // 推荐做法：只读取不修改，或返回一个新的 AmisProperties 对象。
        String env = System.getProperty("spring.profiles.active", "dev");
        AmisProperties copy = new AmisProperties();
        copy.setApp(new AmisProperties.App());
        copy.getApp().setBrandName(properties.getApp().getBrandName() + " [" + env + "]");
        // ... 复制其他需要的字段
        return copy;
    }
}
```

> 多个 `AmisPropertiesCustomizer` 按 `@Order` 顺序依次执行，前一个的输出是后一个的输入。

### 3. 页面自定义器

```java
@Component
public class DynamicPageCustomizer implements AmisPageCustomizer {

    @Override
    public List<AmisProperties.Page> customizePages(
            List<AmisProperties.Page> pages, AmisProperties properties) {
        List<AmisProperties.Page> result = new ArrayList<>(pages);

        // 使用 AmisPages 工厂方法动态追加菜单分组
        result.add(AmisPages.group("动态菜单", "fa fa-bolt",
                AmisPages.children(
                        AmisPages.page("动态页面", "/dynamic", "/api/schema/dynamic", "fa fa-file")
                )));

        return result;
    }
}
```

### 4. 渲染拦截器

```java
@Component
public class AnalyticsInterceptor implements AmisRenderInterceptor {

    @Override
    public void beforeRender(AmisRenderContext context) {
        // context.getTemplateType() 返回 "app" 或 "schema"
        // context.getData() 返回渲染数据 Map
        context.set("trackingId", "G-XXXXXXXXXX");
    }

    @Override
    public String afterRender(AmisRenderContext context, String html) {
        String trackingId = (String) context.get("trackingId");
        if (trackingId != null) {
            String script = "<script>console.log('Analytics:', '" + trackingId + "');</script>";
            return html.replace("</body>", script + "</body>");
        }
        return html;
    }
}
```

> 拦截器抛异常时会被跳过并记录 WARN 日志，不影响渲染。

### 优先级控制

使用 `@Order` 注解控制执行顺序，数值越小优先级越高。多个扩展按顺序依次执行。

## 编程式渲染

在 Controller 或 Service 中注入 `AmisViewService` 进行渲染：

```java
@RestController
public class MyController {

    private final AmisViewService viewService;

    public MyController(AmisViewService viewService) {
        this.viewService = viewService;
    }

    // Schema 模式 — 传入 JSON 字符串
    @GetMapping("/custom-page")
    public ResponseEntity<String> customPage() {
        String schema = "{\"type\":\"page\",\"title\":\"自定义\",\"body\":\"Hello\"}";
        String html = viewService.renderHtml(schema, "自定义页面");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    // Schema 模式 — 带 customCss / customJs
    @GetMapping("/styled-page")
    public ResponseEntity<String> styledPage() {
        String schema = "{\"type\":\"page\",\"body\":\"styled\"}";
        String css = ".banner { background: #667eea; color: white; }";
        String js = "console.log('hello');";
        String html = viewService.renderHtml(schema, "样式页面", css, js);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    // App 模式 — 使用 yml 中配置的 pages
    @GetMapping("/app-render")
    public ResponseEntity<String> appRender() {
        String html = viewService.renderHtml();
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    // App 模式 — 带自定义数据
    @GetMapping("/app-data")
    public ResponseEntity<String> appWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("extraInfo", "动态数据");
        String html = viewService.renderHtml(data);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
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

## AmisPages 工厂方法

`AmisPages` 提供静态方法快速构建 `Page` 对象：

```java
// 简单页面
AmisPages.page("首页", "/home", "/api/schema/home", "fa fa-home");

// 带子页面的页面
AmisPages.page("父页面", "/parent", "/api/schema/parent", "fa fa-sitemap",
        AmisPages.children(
                AmisPages.page("子页面A", "/parent/a", "/api/schema/a", "fa fa-file"),
                AmisPages.page("子页面B", "/parent/b", "/api/schema/b", "fa fa-file")
        ));

// 菜单分组
AmisPages.group("系统管理", "fa fa-cog",
        AmisPages.children(
                AmisPages.page("用户管理", "/users", "/api/schema/users", "fa fa-user"),
                AmisPages.page("角色管理", "/roles", "/api/schema/roles", "fa fa-shield")
        ));

// 辅助方法
AmisPages.children(page1, page2, page3);  // 打包为数组
AmisPages.emptyChildren();                  // 空数组
```

## Bean 替换

Starter 的核心 bean 均使用 `@ConditionalOnMissingBean` 注册，可在项目中定义同类型 bean 替换默认实现：

| 可替换的 Bean | 类型 |
|---------------|------|
| `AmisViewService` | 核心渲染服务 |
| `ClasspathAmisSchemaProvider` | Classpath Schema 解析器 |
| `PropertiesAppSchemaProvider` | App 配置 Schema 解析器 |

## 完整示例

参见 [amisui-starter-example](https://github.com/topxiao/amisui-starter-example) 项目，包含所有功能的可运行示例。

## 开发说明

```bash
# 编译
mvn clean compile

# 测试
mvn test

# 打包
mvn clean package
```

## 许可证

MIT License - see the [LICENSE](LICENSE) file for details.
