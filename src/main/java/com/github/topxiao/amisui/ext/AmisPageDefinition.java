package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisProperties;

import java.util.Map;

/**
 * 页面定义模型。
 * <p>
 * 比 {@link AmisProperties.Page} 更丰富的页面描述，支持内联 schema 和排序。
 * 可在 {@link AmisPageCustomizer} 中使用，最终通过 {@link #toPageConfig()} 转换为渲染用的 Page 对象。
 */
public class AmisPageDefinition {

    private String label;
    private String url;
    private String redirect;
    private String schemaApi;
    private String link;
    private String icon;
    /**
     * 内联 schema（直接嵌入的 amis JSON 配置，优先于 schemaApi）
     */
    private Map<String, Object> schema;
    /**
     * 额外属性（透传给 amis 的自定义字段）
     */
    private Map<String, Object> props;
    /**
     * 排序权重，值越小越靠前，默认 100
     */
    private int order = 100;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getSchemaApi() {
        return schemaApi;
    }

    public void setSchemaApi(String schemaApi) {
        this.schemaApi = schemaApi;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Map<String, Object> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }

    public Map<String, Object> getProps() {
        return props;
    }

    public void setProps(Map<String, Object> props) {
        this.props = props;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * 转换为 {@link AmisProperties.Page}，用于渲染
     */
    public AmisProperties.Page toPageConfig() {
        AmisProperties.Page page = new AmisProperties.Page();
        page.setLabel(this.label);
        page.setUrl(this.url);
        page.setRedirect(this.redirect);
        page.setSchemaApi(this.schemaApi);
        page.setLink(this.link);
        page.setIcon(this.icon);
        return page;
    }
}
