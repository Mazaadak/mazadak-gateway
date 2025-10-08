package com.mazadak.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class UserIdAttachmentFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO add error handling for invalid/missing token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String userId = decodeJwt(token);

            if (userId != null) {
                exchange.getRequest()
                        .mutate()
                        .header("X-User-ID", userId)
                        .build();

            }

        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String decodeJwt(String token) {
        return "1"; // TODO: implement JWT decoding to extract user ID
    }
}
