package com.github.topxiao.amisui.ext;

/**
 * Amis Schema 解析策略接口。
 * <p>
 * 按名称解析 amis 页面的 JSON Schema。所有实现类通过 Spring 注入为有序列表，
 * 解析时按顺序调用，第一个返回非 null 的结果生效。
 * <p>
 * 内置两个实现：
 * <ul>
 *   <li>{@link ClasspathAmisSchemaProvider} — 从 classpath 加载 JSON 文件（优先级高）</li>
 *   <li>{@link PropertiesAppSchemaProvider} — 从配置属性构建 app JSON（优先级低，仅处理 "app"）</li>
 * </ul>
 * 用户可注册自定义实现来覆盖或扩展解析行为。
 */
@FunctionalInterface
public interface AmisSchemaProvider {

    /**
     * 按名称解析 Schema。
     *
     * @param name 视图名称（如 "users"、"app"），即 {@code amis:} 前缀后的部分
     * @return JSON Schema 字符串，如果无法解析则返回 null
     */
    String resolveSchema(String name);
}
