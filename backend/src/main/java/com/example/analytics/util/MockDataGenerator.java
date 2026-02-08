package com.example.analytics.util;

import com.example.analytics.models.UserEvent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class MockDataGenerator implements CommandLineRunner {
    private boolean enabled = true;

    @Override
    public void run(String... args) {
        if (!enabled) return;

        new Thread(() -> {
            RestTemplate restTemplate = new RestTemplate();
            Random random = new Random();
            List<String> users = List.of("UserA", "UserB", "UserC", "UserD", "UserE");
            List<String> pages = List.of("/home", "/products", "/cart", "/checkout", "/login", "/blog");

            //System.out.println("Starting Mock Data Generator...");

            while (true) {
                try {
                    UserEvent event = new UserEvent();
                    event.setUserId(users.get(random.nextInt(users.size())));
                    event.setPageUrl(pages.get(random.nextInt(pages.size())));
                    event.setEventType("page_view");
                    event.setTimestamp(System.currentTimeMillis());
                    event.setSessionId("sess_" + random.nextInt(10)); // Simulate 10 different sessions

                    try {
                        restTemplate.postForLocation("http://localhost:8080/api/events", event);
                    } catch (Exception e) {

                    }

                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}


