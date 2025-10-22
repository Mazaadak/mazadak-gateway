package com.mazadak.gateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {
    private final JwtDecoder jwtDecoder;
    private static final String AUTH_HEADER = "Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var headers = exchange.getRequest().getHeaders();

        headers.remove("X-User-Id");

        List<String> authHeaders = headers.get(AUTH_HEADER);
        if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
            // No token
            return chain.filter(exchange);
        }

        String token = authHeaders.get(0).substring(7);
        return Mono.fromCallable(() -> jwtDecoder.decode(token))
                .flatMap(jwt -> {
                    String userId = jwt.getClaim("user-id");

                    ServerHttpRequest mutated = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id", userId)
                            .build();

                    return chain.filter(exchange
                            .mutate()
                            .request(mutated)
                            .build()
                    );
                }).onErrorResume(ex -> {
                    // invalid token -> reject
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    DataBuffer db = response.bufferFactory()
                            .wrap(("Invalid token: " + ex.getMessage()).getBytes());
                    return response.writeWith(Mono.just(db));
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
