package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisUiProperties;

import java.util.List;
import java.util.Map;

/**
 * Amis UI Page Customizer Interface
 * <p>
 * Implement this interface to customize page configuration
 */
public interface AmisUiPageCustomizer {

    /**
     * Customize pages list
     *
     * @param pages      original pages
     * @param properties global properties
     * @return customized pages
     */
    List<AmisUiProperties.Page> customizePages(List<AmisUiProperties.Page> pages, AmisUiProperties properties);
}
