package com.github.topxiao.amisui;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Amis UI Controller
 */
@RestController
@ConditionalOnProperty(prefix = "amis.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AmisUiController {

    private final AmisUiService amisUiService;

    public AmisUiController(AmisUiService amisUiService) {
        this.amisUiService = amisUiService;
    }

    @GetMapping(value = "${amis.ui.base-path:/amis}", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return amisUiService.renderHtml();
    }

    @GetMapping("${amis.ui.base-path:/amis}/health")
    public String health() {
        return "Amis UI Starter is running";
    }
}
