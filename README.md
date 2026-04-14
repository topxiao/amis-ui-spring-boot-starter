# Amis UI Spring Boot 3 Starter

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.topxiao/amis-ui-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.topxiao/amis-ui-spring-boot-starter)

Amis UI Spring Boot 3 Starter 是一个Spring Boot 3自动配置库，用于快速集成Amis UI到Spring Boot应用中。

## 功能特性

- 🚀 自动集成Amis UI SDK静态资源
- ⚙️ 完整的配置属性支持
- 🎨 可自定义主题和样式
- 📱 响应式设计
- 🔧 灵活的页面配置
- 🌐 RESTful API支持

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

在 `application.yml` 或 `application.properties` 中配置：

```yaml
amis:
  ui:
    enabled: true
    version: "6.12.0"
    api-host: "http://localhost:8080"
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

启动Spring Boot应用后，访问 `http://localhost:8080` 即可看到Amis UI界面。

## 配置属性

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `amis.ui.enabled` | Boolean | `true` | 是否启用Amis UI |
| `amis.ui.version` | String | `"6.12.0"` | Amis SDK版本 |
| `amis.ui.api-host` | String | `""` | API主机地址 |
| `amis.ui.app.brand-name` | String | `"Admin"` | 应用品牌名称 |
| `amis.ui.app.logo` | String | `"img/logo.png"` | 应用Logo路径 |
| `amis.ui.app.title` | String | `"Admin"` | 应用标题 |
| `amis.ui.app.theme` | String | `"ang"` | 应用主题 |
| `amis.ui.custom-css` | String | `""` | 自定义CSS样式 |
| `amis.ui.custom-js` | String | `""` | 自定义JavaScript代码 |

## 页面配置

页面配置支持以下属性：

- `label`: 页面显示名称
- `url`: 页面URL路径
- `redirect`: 重定向URL
- `schema-api`: Amis Schema API地址
- `link`: 外部链接
- `icon`: 页面图标
- `children`: 子页面配置（用于创建多级菜单）

### 示例配置

```yaml
amis:
  ui:
    pages:
      - label: "首页"
        url: "/"
        redirect: "/welcome"
      - label: "数据管理"
        children:
          - label: "用户列表"
            url: "users"
            schema-api: "get:/api/amis/users"
            icon: "fa fa-users"
          - label: "订单管理"
            url: "orders"
            schema-api: "get:/api/amis/orders"
            icon: "fa fa-shopping-cart"
      - label: "系统设置"
        link: "http://external-system.com/settings"
```

## 自定义样式和脚本

可以通过配置添加自定义CSS和JavaScript：

```yaml
amis:
  ui:
    custom-css: |
      .custom-header {
        background-color: #f0f0f0;
      }
    custom-js: |
      console.log('Amis UI loaded with custom script');
```

## 扩展机制

Amis UI Starter 提供了强大的扩展机制，引用方可以通过实现接口来自定义配置和功能。

### 扩展点概述

| 接口 | 描述 | 使用场景 |
|------|------|----------|
| `AmisUiPropertiesCustomizer` | 属性配置自定义器 | 动态修改配置、计算属性值、配置验证 |
| `AmisUiRenderInterceptor` | 渲染拦截器 | 注入脚本、修改HTML、添加Meta标签 |
| `AmisUiPageCustomizer` | 页面自定义器 | 动态添加页面、修改菜单结构 |

### 1. 属性配置自定义器

实现 `AmisUiPropertiesCustomizer` 接口来自定义配置属性：

```java
package com.github.topxiao.amisui.extension;

import com.github.topxiao.amisui.AmisUiProperties;
import com.github.topxiao.amisui.ext.AmisUiPropertiesCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)  // 控制执行顺序，数值越小优先级越高
public class MyPropertiesCustomizer implements AmisUiPropertiesCustomizer {

    @Override
    public AmisUiProperties customize(AmisUiProperties properties) {
        // 根据环境修改配置
        String env = System.getProperty("spring.profiles.active");
        if ("prod".equals(env)) {
            properties.getApp().setTitle("生产环境 - " + properties.getApp().getTitle());
            properties.setApiHost("https://api.production.com");
        }
        return properties;
    }

    @Override
    public boolean supports(String propertyName) {
        // 可选：只处理特定属性
        return "title".equals(propertyName) || "apiHost".equals(propertyName);
    }

    @Override
    public String getName() {
        return "MyPropertiesCustomizer";
    }
}
```

### 2. 渲染拦截器

实现 `AmisUiRenderInterceptor` 接口来拦截HTML渲染过程：

```java
package com.github.topxiao.amisui.extension;

import com.github.topxiao.amisui.ext.AmisUiRenderInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AnalyticsInterceptor implements AmisUiRenderInterceptor {

    @Override
    public void beforeRender(RenderContext context) {
        // 在渲染前添加数据
        context.set("enableAnalytics", true);
    }

    @Override
    public String afterRender(RenderContext context, String html) {
        // 注入分析脚本
        if (Boolean.TRUE.equals(context.get("enableAnalytics"))) {
            String analytics = """
                <script>
                    console.log('Analytics enabled');
                </script>
                """;
            return html.replace("</body>", analytics + "</body>");
        }
        return html;
    }
}
```

### 3. 页面自定义器

实现 `AmisUiPageCustomizer` 接口来动态修改页面配置：

```java
package com.github.topxiao.amisui.extension;

import com.github.topxiao.amisui.AmisUiProperties;
import com.github.topxiao.amisui.ext.AmisUiPageCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class DynamicPageCustomizer implements AmisUiPageCustomizer {

    @Override
    public List<AmisUiProperties.Page> customizePages(
            List<AmisUiProperties.Page> pages, 
            AmisUiProperties properties) {
        
        List<AmisUiProperties.Page> result = new ArrayList<>(pages);
        
        // 动态添加新页面
        AmisUiProperties.Page newPage = new AmisUiProperties.Page();
        newPage.setLabel("动态页面");
        newPage.setUrl("/dynamic");
        newPage.setIcon("fa-cog");
        newPage.setSchemaApi("/api/dynamic-schema");
        result.add(newPage);
        
        return result;
    }

    @Override
    public String getName() {
        return "DynamicPageCustomizer";
    }
}
```

### 4. 非Spring环境使用（SPI）

如果不使用Spring框架，可以通过SPI机制注册扩展：

1. 在 `META-INF/services/com.github.topxiao.amisui.ext.AmisUiExtension` 文件中注册实现类：

```
com.github.topxiao.amisui.extension.MyPropertiesCustomizer=100
com.github.topxiao.amisui.extension.AnalyticsInterceptor=200
com.github.topxiao.amisui.extension.DynamicPageCustomizer=300
```

2. 在代码中手动加载扩展：

```java
import com.github.topxiao.amisui.ext.*;

import java.util.ServiceLoader;

public class ExtensionLoader {
    
    public static void AmisUiExtensionRegistry loadExtensions() {
        DefaultAmisUiExtensionRegistry registry = new DefaultAmisUiExtensionRegistry();
        
        ServiceLoader<AmisUiExtension> loader = 
            ServiceLoader.load(AmisUiExtension.class);
        
        for (AmisUiExtension extension : loader) {
            registry.registerExtension(extension);
        }
        
        return registry;
    }
}
```

### 优先级控制

- 使用 `@Order` 注解控制扩展的执行顺序（数值越小优先级越高）
- 实现 `getOrder()` 方法也可以控制优先级
- 多个扩展会按顺序依次执行

### 最佳实践

1. **单一职责**：每个扩展类只负责一个功能
2. **错误处理**：扩展中应捕获异常，避免影响主流程
3. **空值检查**：对传入的参数进行空值检查
4. **日志记录**：使用日志记录扩展的执行情况

## API接口

### 获取Amis UI页面

```http
GET /
```

返回完整的HTML页面，包含Amis UI应用。

### 健康检查

```http
GET /health
```

返回服务状态信息。

## 开发说明

### 构建项目

```bash
# 安装npm依赖并复制Amis SDK
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package
```

### 本地开发

1. 克隆项目
2. 运行 `mvn clean compile` 安装依赖
3. 在你的Spring Boot项目中引用此starter
4. 启动应用进行测试

## 许可证

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 贡献

欢迎提交Issue和Pull Request！

## 版本历史

### 0.0.2
- 新增扩展机制支持
- 新增 `AmisUiPropertiesCustomizer` 接口，支持自定义配置属性
- 新增 `AmisUiRenderInterceptor` 接口，支持渲染前后拦截
- 新增 `AmisUiPageCustomizer` 接口，支持动态页面配置
- 新增 SPI 支持，可在非Spring环境使用扩展点

### 0.0.1
- 初始版本发布
- 支持基本的Amis UI集成
- 提供完整的配置选项
- 自动资源管理