package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisUiProperties;

import java.util.Map;

/**
 * Amis UI 扩展页面配置
 * <p>
 * 用于动态构建页面的扩展配置类
 *
 * @author zyarn
 * @version 1.0.0
 */
public class AmisUiExtensionPage {

    private String label;
    private String url;
    private String redirect;
    private String schemaApi;
    private String link;
    private String icon;
    private Map<String, Object> schema;
    private Map<String, Object> props;
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
     * 转换为标准Page配置
     */
    public AmisUiProperties.Page toPageConfig() {
        AmisUiProperties.Page page = new AmisUiProperties.Page();
        page.setLabel(this.label);
        page.setUrl(this.url);
        page.setRedirect(this.redirect);
        page.setSchemaApi(this.schemaApi);
        page.setLink(this.link);
        page.setIcon(this.icon);
        return page;
    }
}

