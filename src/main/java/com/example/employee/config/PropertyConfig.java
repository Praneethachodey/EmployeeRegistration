package com.example.employee.config;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped
public class PropertyConfig {

    private final String baseUrl;
    private final int timeoutMs;
    private final String employeeUrl;

    public PropertyConfig() {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in classpath");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }

        this.baseUrl = props.getProperty("microservice.base.url").trim();
        this.timeoutMs = Integer.parseInt(props.getProperty("microservice.timeout.ms").trim());
        this.employeeUrl = props.getProperty("microservice.api").trim();
    }

    public String getEmployeeUrl() {
        return employeeUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }
}
