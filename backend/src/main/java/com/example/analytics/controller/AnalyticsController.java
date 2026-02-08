package com.example.analytics.controller;

import com.example.analytics.models.UserEvent;
import com.example.analytics.service.AnalyticsService;
import com.example.analytics.service.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final RateLimiter rateLimiter;

    public AnalyticsController(AnalyticsService analyticsService, RateLimiter rateLimiter) {
        this.analyticsService = analyticsService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/events")
    public ResponseEntity<?> ingestEvent(@Valid @RequestBody UserEvent event) {
        if (!rateLimiter.tryConsume()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded");
        }
        analyticsService.processEvent(event);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = Map.of(
                "activeUsers", analyticsService.getActiveUserCount(),
                "topPages", analyticsService.getTopPages(),
                "activeSessions", analyticsService.getActiveSessionsByUser()
        );
        return ResponseEntity.ok(stats);
    }
}
