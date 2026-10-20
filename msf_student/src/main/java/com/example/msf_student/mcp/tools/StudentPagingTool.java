package com.example.msf_student.mcp.tools;

import com.example.library_helper.dto.PagingResult;
import com.example.msf_student.dto.StudentDto;
import com.example.msf_student.mcp.helper.McpBearerTokenHelper;
import com.example.msf_student.service.interfaces.StudentOrchestrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP Tool for getting student data with pagination.
 * <p>
 * This tool is exposed via the MCP server and allows MCP clients (e.g., Antigravity, MCP Inspector)
 * to search and retrieve paginated student data from the MSF Student layer.
 * </p>
 * <p>
 * The tool calls {@link StudentOrchestrationService#getPaging} internally,
 * which in turn communicates with the MSM Student microservice.
 * </p>
 */
@Component
public class StudentPagingTool {

    private static final Logger log = LoggerFactory.getLogger(StudentPagingTool.class);

    private final StudentOrchestrationService studentOrchestrationService;
    private final McpBearerTokenHelper mcpBearerTokenHelper;
    private final ObjectMapper objectMapper;

    public StudentPagingTool(StudentOrchestrationService studentOrchestrationService,
                             McpBearerTokenHelper mcpBearerTokenHelper) {
        this.studentOrchestrationService = studentOrchestrationService;
        this.mcpBearerTokenHelper = mcpBearerTokenHelper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * MCP Tool: get_data_student_pagination
     * <p>
     * Retrieves paginated student data based on the provided search criteria.
     * The bearer token for authentication is automatically resolved from:
     * 1. The explicit 'bearerToken' parameter (ideal for MCP Inspector testing)
     * 2. The HTTP Authorization header (automatically sent by Antigravity)
     * 3. The fallback MCP config file (mcp_config.json)
     * </p>
     *
     * @param page             page number (0-based index), default 0
     * @param rows             number of rows per page, default 10
     * @param sortBy           field name to sort by (e.g., "id", "nama", "kelas"), default "id"
     * @param sortDir          sort direction: "asc" or "desc", default "asc"
     * @param valueFilter      the search keyword for filtering student name (e.g., "budi")
     * @param matchModeFilter  how to match the filter: "contains", "startsWith", "endsWith", "equals", default "contains"
     * @param bearerToken      optional Bearer token (e.g. 'Bearer eyJ...'). If provided, this overrides header/config token.
     * @return JSON string with paginated student data
     */
    @Tool(name = "get_data_student_pagination",
          description = "Get student data with pagination. " +
                        "Use this tool to search and retrieve paginated list of students. " +
                        "You can filter by student name, set page number, rows per page, and sorting.")
    public String getDataStudentPagination(
            @ToolParam(description = "Page number (0-based index)", required = false) Integer page,
            @ToolParam(description = "Number of rows per page", required = false) Integer rows,
            @ToolParam(description = "Field name to sort by (e.g., id, nama, kelas)", required = false) String sortBy,
            @ToolParam(description = "Sort direction: asc or desc", required = false) String sortDir,
            @ToolParam(description = "Value filter - the search keyword for filtering student name", required = false) String valueFilter,
            @ToolParam(description = "Match mode filter - how to match: contains, startsWith, endsWith, equals", required = false) String matchModeFilter,
            @ToolParam(description = "Bearer token for authorization (e.g., 'Bearer eyJ...'). Optional if header is already configured.", required = false) String bearerToken) {

        // Apply defaults
        int pageNum = (page != null) ? page : 0;
        int rowsNum = (rows != null) ? rows : 10;
        String sort = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        String dir = (sortDir != null && !sortDir.isBlank()) ? sortDir : "asc";
        String matchMode = (matchModeFilter != null && !matchModeFilter.isBlank()) ? matchModeFilter : "contains";

        // Resolve bearer token: ToolParam > HttpContext (Antigravity header) > Config file
        String resolvedToken = (bearerToken != null && !bearerToken.isBlank())
                ? normalizeBearer(bearerToken)
                : mcpBearerTokenHelper.getBearerToken();

        log.info("[MCP-TOOL] get_data_student_pagination called: page={}, rows={}, sortBy='{}', sortDir='{}', " +
                 "valueFilter='{}', matchModeFilter='{}', hasToken={}",
                 pageNum, rowsNum, sort, dir, valueFilter, matchMode, !resolvedToken.isBlank());

        try {
            // Call StudentOrchestrationService.getPaging()
            PagingResult<StudentDto> result = studentOrchestrationService.getPaging(
                    pageNum, rowsNum, sort, dir, valueFilter, matchMode, resolvedToken);

            // Convert result to JSON string for MCP response
            String jsonResult = objectMapper.writeValueAsString(result);

            log.info("[MCP-TOOL] get_data_student_pagination returned {} records (page {}/{})",
                     result.getData() != null ? result.getData().size() : 0,
                     result.getPageNumber(),
                     result.getTotalPages());

            return jsonResult;

        } catch (JsonProcessingException e) {
            log.error("[MCP-TOOL] Error serializing paging result to JSON: {}", e.getMessage());
            return "{\"error\": \"Failed to serialize student paging result: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("[MCP-TOOL] Error executing get_data_student_pagination: {}", e.getMessage());
            return "{\"error\": \"Failed to get student data: " + e.getMessage() + "\"}";
        }
    }

    private String normalizeBearer(String token) {
        String trimmed = token.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (!trimmed.toLowerCase().startsWith("bearer ")) {
            return "Bearer " + trimmed;
        }
        return trimmed;
    }
}
