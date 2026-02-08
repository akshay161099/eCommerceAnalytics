package com.example.analytics.service;

import com.example.analytics.models.UserEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final StringRedisTemplate redisTemplate;
    private static final String KEY_ACTIVE_USERS = "metrics:active_users";
    private static final String KEY_PAGE_VIEWS = "metrics:page_views";
    private static final String KEY_SESSIONS = "metrics:sessions";

    public AnalyticsService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void processEvent(UserEvent event) {
        long now = System.currentTimeMillis();

        // 1. Active Users (Rolling 5 mins)
        // Store UserID with Score = Timestamp
        redisTemplate.opsForZSet().add(KEY_ACTIVE_USERS, event.getUserId(), now);

        // 2. Page Views (Rolling 15 mins)
        // We need unique members to store every view. Format: url::uuid
        String uniqueViewParams = event.getPageUrl() + "::" + UUID.randomUUID();
        redisTemplate.opsForZSet().add(KEY_PAGE_VIEWS, uniqueViewParams, now);

        // 3. Active Sessions (Rolling 5 mins)
        // Store SessionID::UserID with Score = Timestamp
        String sessionKey = (event.getSessionId() != null ? event.getSessionId() : "unknown") + "::" + event.getUserId();
        redisTemplate.opsForZSet().add(KEY_SESSIONS, sessionKey, now);

        // Cleanup old data (Async or Lazy - doing lazy here for simplicity)
        cleanup(now);
    }

    private void cleanup(long now) {
        long fiveMinAgo = now - (5 * 60 * 1000);
        long fifteenMinAgo = now - (15 * 60 * 1000);

        redisTemplate.opsForZSet().removeRangeByScore(KEY_ACTIVE_USERS, 0, fiveMinAgo);
        redisTemplate.opsForZSet().removeRangeByScore(KEY_SESSIONS, 0, fiveMinAgo);
        redisTemplate.opsForZSet().removeRangeByScore(KEY_PAGE_VIEWS, 0, fifteenMinAgo);
    }

    // --- Analytics Getters ---

    public long getActiveUserCount() {
        return Optional.ofNullable(redisTemplate.opsForZSet().zCard(KEY_ACTIVE_USERS)).orElse(0L);
    }

    public Map<String, Long> getTopPages() {

        long now = System.currentTimeMillis();
        long fifteenMinAgo = now - (15 * 60 * 1000);

        Set<String> views = redisTemplate.opsForZSet().rangeByScore(KEY_PAGE_VIEWS, fifteenMinAgo, now);

        if (views == null) return Collections.emptyMap();


        return views.stream()
                .map(v -> v.split("::")[0]) // Extract URL
                .collect(Collectors.groupingBy(url -> url, Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Sort Desc
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Long> getActiveSessionsByUser() {

        Set<String> sessions = redisTemplate.opsForZSet().range(KEY_SESSIONS, 0, -1);
        if (sessions == null) return Collections.emptyMap();

        return sessions.stream()
                .map(s -> s.split("::")[1]) // Extract UserID
                .collect(Collectors.groupingBy(user -> user, Collectors.counting()));
    }
}
