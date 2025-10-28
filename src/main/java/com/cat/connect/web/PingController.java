package com.cat.connect.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
public class PingController {

    @Value("${spring.application.name:CatConnect}")
    private String appName;

    @Value("${build.version:dev}")
    private String buildVersion; // optionally from build-info

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("app", appName, "version", buildVersion, "status", "ok", "time", OffsetDateTime.now().toString());
    }
}
