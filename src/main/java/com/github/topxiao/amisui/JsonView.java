package com.github.topxiao.amisui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.View;

import java.io.IOException;
import java.util.Map;

public class JsonView implements View {

    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    private final String jsonContent;

    public JsonView(String jsonContent) {
        this.jsonContent = jsonContent;
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.getWriter().write(jsonContent);
    }
}
