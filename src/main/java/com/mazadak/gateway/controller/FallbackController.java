package com.mazadak.gateway.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @RequestMapping
    public ProblemDetail defaultFallback(ServerWebExchange exchange) {
        String requestUri = exchange.getRequest().getURI().toString();
        log.error("Fallback triggered for request: {}", requestUri);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problemDetail.setTitle("Service Unavailable");
        problemDetail.setDetail("A downstream service is temporarily unavailable. Please try again later.");
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setInstance(URI.create(requestUri));
        return problemDetail;
    }

}
