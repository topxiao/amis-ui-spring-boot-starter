package com.github.topxiao.amisui.ext;

import com.github.topxiao.amisui.AmisProperties;

import java.util.List;

public interface AmisPageCustomizer {
    List<AmisProperties.Page> customizePages(List<AmisProperties.Page> pages, AmisProperties properties);
}
