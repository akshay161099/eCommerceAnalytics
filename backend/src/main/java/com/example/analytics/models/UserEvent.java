package com.example.analytics.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserEvent {

    @NotNull(message = "Timestamp is required")
    private Long timestamp;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotBlank(message = "Page URL is required")
    private String pageUrl;

    private String sessionId;

}
