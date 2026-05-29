# Amis UI Spring Boot 3 Starter

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.topxiao/amis-ui-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.topxiao/amis-ui-spring-boot-starter)

Spring Boot 3 Starter，用于快速集成 [Amis](https://aisuda.bce.baidu.com/amis/zh-CN/docs/index) 低代码前端框架。

## 功能特性

- 自动集成 Amis SDK 静态资源
- Spring MVC 原生视图解析（ViewControllerMapping + ViewResolver）
- `@ConfigurationProperties` 类型安全配置
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
  app:
    brand-name: "My Admin"
    logo: "/img/logo.png"
    title: "管理系统"
    theme: "ang"
  pages:
    - label: "首页"
      url: "/"
      redirect: "/dashboard"
    - label: "仪表板"
      url: "dashboard"
      schema-api: "get:/pages/dashboard.json"
    - label: "系统管理"
      children:
        - label: "用户管理"
          url: "users"
          schema-api: "get:/pages/users.json"
        - label: "角色管理"
          url: "roles"
          schema-api: "get:/pages/roles.json"
```

### 3. 启动应用

启动后访问 `http://localhost:8080/amis` 即可看到 Amis UI 界面。

## 配置属性

所有配置前缀为 `amis`。

### 顶层属性

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `amis.enabled` | Boolean | `true` | 是否启用 Amis UI |
| `amis.path` | String | `"/amis"` | Amis 页面映射路径 |
| `amis.version` | String | `"6.12.0"` | Amis SDK 版本 |
| `amis.ctx` | String | `""` | 应用上下文路径 |

### App 属性

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `amis.app.brand-name` | String | `"Admin"` | 应用品牌名称 |
| `amis.app.logo` | String | `"img/logo.png"` | 应用 Logo 路径 |
| `amis.app.title` | String | `"Admin"` | 应用标题 |
| `amis.app.theme` | String | `"ang"` | 应用主题 |
| `amis.app.header` | List<Map> | `null` | 顶部导航配置 |

### 页面属性

页面配置支持以下字段：

- `label`: 页面显示名称
- `url`: 页面 URL 路径
- `redirect`: 重定向 URL
- `schema-api`: Amis Schema API 地址
- `link`: 外部链接
- `icon`: 页面图标
- `children`: 子页面（用于创建多级菜单）

## 扩展机制

Starter 通过 Spring `List<T>` 构造器注入收集扩展，注册 `@Component` 即可自动生效。

### 扩展点

| 接口 | 方法 | 使用场景 |
|------|------|----------|
| `AmisPropertiesCustomizer` | `customize(AmisProperties)` | 动态修改配置属性 |
| `AmisPageCustomizer` | `customizePages(List<Page>, AmisProperties)` | 动态添加/修改页面 |
| `AmisRenderInterceptor` | `beforeRender(ctx)` / `afterRender(ctx, html)` | 拦截渲染过程 |

### 1. 属性配置自定义器

```java
@Component
@Order(1)
public class MyPropertiesCustomizer implements AmisPropertiesCustomizer {

    @Override
    public AmisProperties customize(AmisProperties properties) {
        String env = System.getProperty("spring.profiles.active");
        if ("prod".equals(env)) {
            properties.getApp().setTitle("生产环境 - " + properties.getApp().getTitle());
        }
        return properties;
    }
}
```

### 2. 页面自定义器

```java
@Component
@Order(1)
public class DynamicPageCustomizer implements AmisPageCustomizer {

    @Override
    public List<AmisProperties.Page> customizePages(
            List<AmisProperties.Page> pages, AmisProperties properties) {
        List<AmisProperties.Page> result = new ArrayList<>(pages);

        AmisProperties.Page newPage = new AmisProperties.Page();
        newPage.setLabel("动态页面");
        newPage.setUrl("/dynamic");
        newPage.setSchemaApi("/api/dynamic-schema");
        result.add(newPage);

        return result;
    }
}
```

### 3. 渲染拦截器

```java
@Component
@Order(1)
public class AnalyticsInterceptor implements AmisRenderInterceptor {

    @Override
    public void beforeRender(AmisRenderContext context) {
        context.set("enableAnalytics", true);
    }

    @Override
    public String afterRender(AmisRenderContext context, String html) {
        if (Boolean.TRUE.equals(context.get("enableAnalytics"))) {
            String script = "<script>console.log('Analytics enabled');</script>";
            return html.replace("</body>", script + "</body>");
        }
        return html;
    }
}
```

### 优先级控制

使用 `@Order` 注解控制执行顺序，数值越小优先级越高。多个扩展按顺序依次执行，某个扩展抛异常时会被跳过并记录警告日志。

## 编程式渲染

在 Controller 或 Service 中注入 `AmisViewService` 进行渲染：

```java
@RestController
public class MyController {

    private final AmisViewService viewService;

    public MyController(AmisViewService viewService) {
        this.viewService = viewService;
    }

    @GetMapping("/custom-page")
    public ResponseEntity<String> customPage() {
        String schema = "{\"type\":\"page\",\"title\":\"自定义页面\",\"body\":\"Hello\"}";
        String html = viewService.renderHtml(schema, "自定义页面");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
```

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
