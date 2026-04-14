package com.github.topxiao.amisui.ext;

/**
 * Amis UI 扩展点接口标记
 * <p>
 * 所有扩展接口都需要继承此接口，以便于识别和管理
 *
 * @author zyarn
 * @version 1.0.0
 */
public interface AmisUiExtension {

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
}

