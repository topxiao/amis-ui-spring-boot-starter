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

```yaml
amis:
  path: /admin
  version: "6.12.0"
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
      url: "/"
      redirect: "/dashboard"
      icon: "fa fa-home"
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
    - label: "系统设置"
      url: "/settings"
      schema-api: "get:/api/pages/settings"
      icon: "fa fa-cog"
    - label: "外部系统"
      link: "https://example.com"
      icon: "fa fa-external-link"
```

启动后访问 `http://localhost:8080/admin`，Amis 渲染完整的管理后台框架。

> **schema-api 说明**：Amis 前端通过 `schema-api` 指定的 URL 动态拉取页面 Schema JSON。你需要自己实现对应的 API 端点返回 Schema。

---

## 3. 自定义访问路径

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

## 4. Controller 中编程式渲染

当你需要在 Controller 中返回 Amis 页面（非菜单模式），注入 `AmisViewService`。

### 4.1 返回 Schema 页面

```java
@Controller
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

### 4.2 返回 App 模式页面

```java
@GetMapping("/custom-app")
public ResponseEntity<String> customApp() {
    // 使用 YAML 中配置的 pages 渲染完整 App
    Map<String, Object> data = new HashMap<>();
    data.put("extraInfo", "动态注入数据");

    String html = viewService.renderHtml(data);
    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
}
```

---

## 5. Spring MVC 视图名方式渲染

Starter 注册了 `AmisViewResolver`，解析 `amis:` 前缀视图名。可以在 Controller 中返回视图名：

```java
@Controller
public class ViewController {

    // 返回 amis:app 视图名 → 渲染完整 App 框架
    @GetMapping("/my-admin")
    public String admin() {
        return "amis:app";
    }

    // 返回 amis:page 视图名 → 渲染 schema 单页
    @GetMapping("/simple-page")
    public String simplePage(Model model) {
        model.addAttribute("schema", """
            {
              "type": "page",
              "body": "Hello from amis:page"
            }
            """);
        model.addAttribute("title", "简单页面");
        return "amis:page";
    }
}
```

> `amis:app` 从 YAML 配置的 `pages` 构建菜单。`amis:page` 从 Model 的 `schema` 属性读取 JSON。

---

## 6. 扩展：动态修改配置属性

```java
import com.github.topxiao.amisui.AmisProperties;
import com.github.topxiao.amisui.ext.AmisPropertiesCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class EnvPropertiesCustomizer implements AmisPropertiesCustomizer {

    @Override
    public AmisProperties customize(AmisProperties properties) {
        // 根据环境变量动态修改 brandName
        String env = System.getProperty("spring.profiles.active", "dev");
        properties.getApp().setBrandName(
            properties.getApp().getBrandName() + " [" + env.toUpperCase() + "]"
        );
        return properties;
    }
}
```

多个 `AmisPropertiesCustomizer` 按 `@Order` 顺序依次执行，前一个的输出是后一个的输入。

---

## 7. 扩展：动态添加页面

```java
import com.github.topxiao.amisui.AmisProperties;
import com.github.topxiao.amisui.ext.AmisPageCustomizer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DynamicPageCustomizer implements AmisPageCustomizer {

    @Override
    public List<AmisProperties.Page> customizePages(
            List<AmisProperties.Page> pages, AmisProperties properties) {
        List<AmisProperties.Page> result = new ArrayList<>(pages);

        // 根据权限动态添加页面
        if (hasAdminRole()) {
            AmisProperties.Page admin = new AmisProperties.Page();
            admin.setLabel("管理面板");
            admin.setUrl("/admin-panel");
            admin.setSchemaApi("get:/api/pages/admin-panel");
            admin.setIcon("fa fa-shield");
            result.add(admin);
        }

        return result;
    }

    private boolean hasAdminRole() {
        // 你的权限判断逻辑
        return true;
    }
}
```

---

## 8. 扩展：渲染拦截器

```java
import com.github.topxiao.amisui.ext.AmisRenderContext;
import com.github.topxiao.amisui.ext.AmisRenderInterceptor;
import org.springframework.stereotype.Component;

@Component
public class GaTrackerInterceptor implements AmisRenderInterceptor {

    @Override
    public void beforeRender(AmisRenderContext context) {
        // 在渲染前设置上下文数据
        context.set("trackingId", "G-XXXXXXXXXX");
    }

    @Override
    public String afterRender(AmisRenderContext context, String html) {
        // 在渲染后注入 Google Analytics 脚本
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

## 9. 使用 AmisPages 工具类

`AmisPages` 提供静态方法快速构建 `Page` 对象：

```java
import com.github.topxiao.amisui.AmisPages;
import com.github.topxiao.amisui.AmisProperties;

import java.util.List;

// 构建单个页面
AmisProperties.Page userPage = AmisPages.page(
    "用户管理",       // label
    "/users",        // url
    "get:/api/pages/users",  // schemaApi
    "fa fa-users"    // icon
);

// 构建带子页面的分组
AmisProperties.Page systemGroup = AmisPages.group(
    "系统管理",       // label
    "fa fa-cog",     // icon
    AmisPages.children(
        AmisPages.page("角色管理", "/roles", "get:/api/pages/roles", "fa fa-key"),
        AmisPages.page("日志查看", "/logs", "get:/api/pages/logs", "fa fa-list")
    )
);

List<AmisProperties.Page> pages = List.of(userPage, systemGroup);
```

---

## 10. 带上下文路径部署

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
  ctx: ""                   # 上下文路径（默认自动读取）
  app:
    brand-name: "Admin"     # 品牌名（默认 Admin）
    logo: "img/logo.png"    # Logo（默认 img/logo.png）
    title: "Admin"          # 标题（默认 Admin）
    theme: "ang"            # 主题（默认 ang）
    header: []              # 顶部导航配置（可选）
  pages: []                 # 菜单页面列表（可选）
```
