package com.sdv.nosql.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdv.nosql.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public LoginResponse login(String userId) {
        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "session:" + token,
                userId,
                Duration.ofSeconds(900)
        );
        Map<String, Object> message = Map.of(
                "userId", userId,
                "event", "LOGIN"
        );

        try {
            redisTemplate.convertAndSend(
                    "users:new",
                    objectMapper.writeValueAsString(message)
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur publication Redis");
        }

        return new LoginResponse(token, 900);
    }
}
