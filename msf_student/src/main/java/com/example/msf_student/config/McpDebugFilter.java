package com.example.msf_student.config;

import com.example.msf_student.mcp.helper.McpContextHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class McpDebugFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(McpDebugFilter.class);
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.contains("/mcp")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Dynamic CORS headers (essential for browser-based MCP Inspector)
        String origin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", (origin != null && !origin.isBlank()) ? origin : "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 2. Handle preflight OPTIONS immediately with 200 OK (matching .NET McpAuthorizationMiddleware)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 3. Extract and store auth token into McpContextHolder
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            authHeader = request.getHeader("authorization");
        }
        if (authHeader == null || authHeader.isBlank()) {
            authHeader = request.getHeader("accessToken");
        }
        if (authHeader == null || authHeader.isBlank()) {
            authHeader = request.getHeader("access-token");
        }
        if (authHeader == null || authHeader.isBlank()) {
            authHeader = request.getHeader("x-user-token");
        }
        if (authHeader != null && !authHeader.isBlank()) {
            McpContextHolder.setToken(authHeader);
            log.debug("[MCP-DEBUG] Inbound token captured into McpContextHolder from header");
        }

        // 4. Read request body
        byte[] bodyBytes = request.getInputStream().readAllBytes();

        // 5. Sanitize initialize request capabilities for Java McpSchema compatibility
        // (Antigravity sends 'elicitation.form', MCP Inspector 2.11 sends 'tasks', 'extensions', 'elicitation.url')
        if (bodyBytes.length > 0) {
            try {
                JsonNode root = jsonMapper.readTree(bodyBytes);
                if (root.isObject() && "initialize".equals(root.path("method").asText())) {
                    JsonNode params = root.path("params");
                    if (params.isObject() && params.has("capabilities")) {
                        ObjectNode capsNode = (ObjectNode) params.get("capabilities");

                        // Sanitize 'elicitation' to empty object {} if present (Java record has 0 components)
                        if (capsNode.has("elicitation")) {
                            capsNode.putObject("elicitation");
                        }

                        // Remove capabilities not recognized by io.modelcontextprotocol.spec.McpSchema$ClientCapabilities
                        // Valid record components: "experimental", "roots", "sampling", "elicitation"
                        List<String> toRemove = new ArrayList<>();
                        Iterator<String> fieldNames = capsNode.fieldNames();
                        while (fieldNames.hasNext()) {
                            String fieldName = fieldNames.next();
                            if (!fieldName.equals("experimental") &&
                                !fieldName.equals("roots") &&
                                !fieldName.equals("sampling") &&
                                !fieldName.equals("elicitation")) {
                                toRemove.add(fieldName);
                            }
                        }
                        for (String field : toRemove) {
                            capsNode.remove(field);
                        }

                        bodyBytes = jsonMapper.writeValueAsBytes(root);
                        log.info("[MCP-DEBUG] Sanitized initialize capabilities for McpSchema compatibility: {}", capsNode);
                    }
                }
            } catch (Exception e) {
                log.warn("[MCP-DEBUG] Could not parse/sanitize JSON body: {}", e.getMessage());
            }
        }

        final byte[] finalBodyBytes = bodyBytes;
        final String finalBodyStr = new String(finalBodyBytes, StandardCharsets.UTF_8);

        // 6. Wrap request to adapt headers and supply cached/sanitized body stream
        HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public ServletInputStream getInputStream() {
                ByteArrayInputStream bais = new ByteArrayInputStream(finalBodyBytes);
                return new ServletInputStream() {
                    @Override
                    public boolean isFinished() {
                        return bais.available() == 0;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(ReadListener readListener) {
                    }

                    @Override
                    public int read() {
                        return bais.read();
                    }
                };
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(finalBodyBytes), StandardCharsets.UTF_8));
            }

            @Override
            public int getContentLength() {
                return finalBodyBytes.length;
            }

            @Override
            public long getContentLengthLong() {
                return finalBodyBytes.length;
            }

            @Override
            public String getHeader(String name) {
                if ("accept".equalsIgnoreCase(name)) {
                    if ("GET".equalsIgnoreCase(getMethod())) {
                        return "text/event-stream";
                    }
                    return "application/json, text/event-stream";
                }
                if ("authorization".equalsIgnoreCase(name)) {
                    String auth = super.getHeader(name);
                    if (auth != null && !auth.isEmpty() && !auth.startsWith("Bearer ") && !auth.startsWith("bearer ")) {
                        return "Bearer " + auth;
                    }
                    return auth;
                }
                if ("mcp-session-id".equalsIgnoreCase(name)) {
                    String sid = super.getHeader(name);
                    if (sid == null || sid.isEmpty()) {
                        return getParameter("sessionId");
                    }
                    return sid;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if ("accept".equalsIgnoreCase(name)) {
                    if ("GET".equalsIgnoreCase(getMethod())) {
                        return Collections.enumeration(List.of("text/event-stream"));
                    }
                    return Collections.enumeration(List.of("application/json", "text/event-stream"));
                }
                if ("authorization".equalsIgnoreCase(name)) {
                    String auth = super.getHeader(name);
                    if (auth != null && !auth.isEmpty()) {
                        if (!auth.startsWith("Bearer ") && !auth.startsWith("bearer ")) {
                            return Collections.enumeration(List.of("Bearer " + auth));
                        }
                        return Collections.enumeration(List.of(auth));
                    }
                }
                if ("mcp-session-id".equalsIgnoreCase(name)) {
                    String sid = super.getHeader(name);
                    if (sid == null || sid.isEmpty()) {
                        String paramSid = getParameter("sessionId");
                        if (paramSid != null && !paramSid.isEmpty()) {
                            return Collections.enumeration(List.of(paramSid));
                        }
                    }
                }
                return super.getHeaders(name);
            }
        };

        log.info("[MCP-DEBUG] >>> {} {}", request.getMethod(), uri);
        try {
            filterChain.doFilter(wrapped, response);
        } finally {
            log.info("[MCP-DEBUG] <<< {} {} -> Status: {}", request.getMethod(), uri, response.getStatus());
        }
    }
}
