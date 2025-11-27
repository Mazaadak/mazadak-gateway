package com.mazadak.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtDecoder jwtDecoder;
    private static final String AUTH_HEADER = "Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var headers = exchange.getRequest().getHeaders();

        // remove any preexisting X-User-Id (to avoid spoofing)
        headers.remove("X-User-Id");

        List<String> authHeaders = headers.get(AUTH_HEADER);
        if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
            log.info("No auth header found, passing request down the chain...");
            return chain.filter(exchange);
        }

        String token = authHeaders.get(0).substring(7);

        return Mono.fromCallable(() -> {
                    // decode and verify token signature using configured JwtDecoder
                    Jwt jwt = jwtDecoder.decode(token);
                    return jwt;
                })
                .flatMap(jwt -> {
                    log.info("A JWT token was found, attaching X-User-Id header.");
                    String userId = jwt.getClaim("user-id");

                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id", userId)
                            .build();

                    log.info("Attached X-User-Id with value: {}", userId);
                    log.info("Final request URI: {}", mutatedRequest.getURI());

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(ex -> {
                    log.error("Error decoding JWT in Gateway. Request={}, Exception={}", exchange.getRequest().getURI(), ex.getMessage(), ex);
                    // pass the request anyway to let downstream handle unauthenticated users
                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
