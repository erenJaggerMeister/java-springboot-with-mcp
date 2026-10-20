package com.example.msf_student.mcp.extension;

import com.example.msf_student.mcp.tools.StudentPagingTool;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP Server Extension configuration.
 * <p>
 * This class acts as the bridge/extension between the MCP middleware and the MSF Student
 * application. It registers MCP tool callbacks so that the Spring AI MCP Server can
 * discover and expose the tools (e.g., get_data_student_pagination) to MCP clients
 * such as Antigravity and the MCP Inspector.
 * </p>
 */
@Configuration
public class McpServerExtension {

    private static final Logger log = LoggerFactory.getLogger(McpServerExtension.class);

    private static final List<String> SUPPORTED_PROTOCOL_VERSIONS = List.of(
            "2024-11-05",
            "2025-06-18",
            "2025-11-25"
    );

    @JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class IgnoreUnknownMixin {}

    /**
     * Registers the StudentPagingTool as an MCP tool callback provider.
     * This allows the MCP server to discover methods annotated with @Tool
     * inside StudentPagingTool and expose them to MCP clients.
     *
     * @param studentPagingTool the tool bean containing get_data_student_pagination
     * @return ToolCallbackProvider that exposes the tool to MCP
     */
    @Bean
    public ToolCallbackProvider studentPagingToolCallbackProvider(StudentPagingTool studentPagingTool) {
        log.info("[MCP-EXTENSION] Registering StudentPagingTool as MCP tool callback provider");
        return MethodToolCallbackProvider.builder()
                .toolObjects(studentPagingTool)
                .build();
    }

    /**
     * Registers protocol versions used by Antigravity Client and standard MCP clients
     * ('2024-11-05', '2025-06-18', '2025-11-25') to ensure successful initialization handshake negotiation.
     * Also configures MCP JsonMapper to ignore unknown client properties (such as 'elicitation.form').
     */
    @EventListener(ApplicationReadyEvent.class)
    public void configureProtocolVersions(ApplicationReadyEvent event) {
        // 1. Configure global McpJsonInternal mapper to ignore unknown properties
        configureGlobalJsonMapper();

        boolean configured = false;

        // 2. Try McpStatelessSyncServer (stateless mode)
        try {
            String[] statelessBeans = event.getApplicationContext().getBeanNamesForType(
                    Class.forName("io.modelcontextprotocol.server.McpStatelessSyncServer")
            );
            if (statelessBeans.length > 0) {
                Object statelessSync = event.getApplicationContext().getBean(statelessBeans[0]);
                Field asyncField = statelessSync.getClass().getDeclaredField("asyncServer");
                asyncField.setAccessible(true);
                Object asyncServer = asyncField.get(statelessSync);
                setProtocolVersionsOnAsync(asyncServer);
                log.info("[MCP-EXTENSION] Successfully registered protocol versions {} on McpStatelessSyncServer", SUPPORTED_PROTOCOL_VERSIONS);
                configured = true;
            }
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            log.warn("[MCP-EXTENSION] Note: Could not configure protocol versions via McpStatelessSyncServer: {}", e.getMessage());
        }

        // 3. Try McpStatelessAsyncServer directly
        if (!configured) {
            try {
                String[] asyncBeans = event.getApplicationContext().getBeanNamesForType(
                        Class.forName("io.modelcontextprotocol.server.McpStatelessAsyncServer")
                );
                if (asyncBeans.length > 0) {
                    Object asyncServer = event.getApplicationContext().getBean(asyncBeans[0]);
                    setProtocolVersionsOnAsync(asyncServer);
                    log.info("[MCP-EXTENSION] Successfully registered protocol versions {} on McpStatelessAsyncServer directly", SUPPORTED_PROTOCOL_VERSIONS);
                    configured = true;
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                log.warn("[MCP-EXTENSION] Note: Could not configure protocol versions via McpStatelessAsyncServer: {}", e.getMessage());
            }
        }

        // 4. Fallback to McpSyncServer (stateful mode if used)
        if (!configured) {
            try {
                String[] syncBeans = event.getApplicationContext().getBeanNamesForType(
                        Class.forName("io.modelcontextprotocol.server.McpSyncServer")
                );
                if (syncBeans.length > 0) {
                    Object syncServer = event.getApplicationContext().getBean(syncBeans[0]);
                    Method getAsyncMethod = syncServer.getClass().getMethod("getAsyncServer");
                    Object asyncServer = getAsyncMethod.invoke(syncServer);
                    setProtocolVersionsOnAsync(asyncServer);
                    log.info("[MCP-EXTENSION] Successfully registered protocol versions {} on McpSyncServer", SUPPORTED_PROTOCOL_VERSIONS);
                    configured = true;
                }
            } catch (ClassNotFoundException ignored) {
            } catch (Exception e) {
                log.warn("[MCP-EXTENSION] Note: Could not configure protocol versions via McpSyncServer: {}", e.getMessage());
            }
        }
    }

    private void configureGlobalJsonMapper() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            mapper.addMixIn(Object.class, IgnoreUnknownMixin.class);
            JacksonMcpJsonMapper customMapper = new JacksonMcpJsonMapper(mapper);

            Class<?> internalClass = Class.forName("io.modelcontextprotocol.json.McpJsonInternal");
            Field defaultMapperField = internalClass.getDeclaredField("defaultJsonMapper");
            defaultMapperField.setAccessible(true);
            defaultMapperField.set(null, customMapper);
            log.info("[MCP-EXTENSION] Successfully injected tolerant JacksonMcpJsonMapper into McpJsonInternal");
        } catch (Exception e) {
            log.warn("[MCP-EXTENSION] Note: Could not inject custom defaultJsonMapper into McpJsonInternal: {}", e.getMessage());
        }
    }

    private void setProtocolVersionsOnAsync(Object asyncServer) throws Exception {
        Field field = asyncServer.getClass().getDeclaredField("protocolVersions");
        field.setAccessible(true);
        field.set(asyncServer, new ArrayList<>(SUPPORTED_PROTOCOL_VERSIONS));
    }
}
