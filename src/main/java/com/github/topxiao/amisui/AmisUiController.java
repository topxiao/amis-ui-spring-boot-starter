package com.github.topxiao.amisui;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Amis UI Controller
 */
@RestController
@RequestMapping("/")
public class AmisUiController {

    private final AmisUiService amisUiService;

    public AmisUiController(AmisUiService amisUiService) {
        this.amisUiService = amisUiService;
    }

    /**
     * Serve the main Amis UI page
     *
     * @return HTML content
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return amisUiService.renderHtml();
    }

    /**
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public String health() {
        return "Amis UI Starter is running";
    }
}