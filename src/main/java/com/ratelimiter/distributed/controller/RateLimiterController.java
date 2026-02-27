package com.ratelimiter.distributed.controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ratelimiter.distributed.service.RateLimiterService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class RateLimiterController {

    private RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }
    
    @GetMapping("/api")
    public ResponseEntity<?> apiEndPoint(@RequestParam String userId) {
        if (rateLimiterService.isAllowed(userId)) {
            return ResponseEntity.ok("Request successful");
        } else {
            return ResponseEntity.status(429).body("Too Many Requests");
        }
    }
}