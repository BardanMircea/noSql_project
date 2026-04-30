package com.sdv.nosql.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdv.nosql.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int SESSION_TTL_SECONDS = 900;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public LoginResponse login(String userId) {
        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "session:" + token,
                userId,
                Duration.ofSeconds(SESSION_TTL_SECONDS)
        );

        try {
            redisTemplate.convertAndSend("users:new", objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "event", "LOGIN"
            )));
        } catch (JsonProcessingException e) {
            log.warn("Failed to publish login event for user {}", userId, e);
        }

        return new LoginResponse(token, SESSION_TTL_SECONDS);
    }
}
