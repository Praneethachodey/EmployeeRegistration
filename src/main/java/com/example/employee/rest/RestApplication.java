package com.example.employee.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api") // Base URI for all endpoints
public class RestApplication extends Application {
    // No need to override anything
}

