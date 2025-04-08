package com.monikit.starter.webflux.filter;

import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import com.monikit.core.TraceIdProvider;

public class TraceIdGatewayFilter implements GatewayFilter {

    private final TraceIdProvider traceIdProvider;

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    public TraceIdGatewayFilter(TraceIdProvider traceIdProvider) {
        this.traceIdProvider = traceIdProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);

        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        traceIdProvider.setTraceId(traceId);

        exchange.getResponse().getHeaders().add(TRACE_ID_HEADER, traceId);

        return chain.filter(exchange)
            .doFinally(signalType -> traceIdProvider.clear());
    }
}
