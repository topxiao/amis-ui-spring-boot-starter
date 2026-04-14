package com.github.topxiao.amisui.ext;

import java.util.Map;

/**
 * Amis UI 渲染拦截器接口
 * <p>
 * 引用方可以实现此接口来拦截和修改HTML渲染过程
 * <p>
 * 使用场景：
 * - 在HTML渲染前后添加自定义内容
 * - 修改页面标题、Meta标签
 * - 添加自定义CSS/JS
 * - 注入第三方脚本
 * - 修改页面结构
 *
 * @author zyarn
 * @version 1.0.0
 */
public interface AmisUiRenderInterceptor {

    /**
     * 获取扩展点名称
     *
     * @return 扩展点名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 获取扩展点优先级
     * 数值越小，优先级越高
     *
     * @return 优先级，默认为0
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 在HTML模板渲染前调用
     *
     * @param context 渲染上下文
     */
    default void beforeRender(RenderContext context) {
    }

    /**
     * 在HTML模板渲染后调用
     *
     * @param context 渲染上下文
     * @param html    渲染后的HTML内容
     * @return 修改后的HTML内容
     */
    default String afterRender(RenderContext context, String html) {
        return html;
    }

    /**
     * 渲染上下文
     */
    class RenderContext {
        private final Map<String, Object> data;
        private final String templateType;

        public RenderContext(Map<String, Object> data, String templateType) {
            this.data = data;
            this.templateType = templateType;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public String getTemplateType() {
            return templateType;
        }

        public Object get(String key) {
            return data.get(key);
        }

        public void set(String key, Object value) {
            data.put(key, value);
        }
    }
}

