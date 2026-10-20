package com.example.msf_student.mcp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * DTO representing the input parameters for the MCP tool get_data_student_pagination.
 * This serves as the container for pagination, sorting, and filter parameters
 * received from the MCP Inspector tool.
 */
public class McpStudentPagingRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Page number (0-based index). */
    private int page = 0;

    /** Number of rows per page. */
    private int rows = 10;

    /** Field name to sort by (e.g., "id", "nama", "kelas"). */
    private String sortBy = "id";

    /** Sort direction: "asc" or "desc". */
    private String sortDir = "asc";

    /** Value filter – the search keyword for filtering student data. */
    @JsonProperty("value filter")
    private String valueFilter;

    /** Match mode filter – how to match the filter value (e.g., "contains", "startsWith", "endsWith", "equals"). */
    @JsonProperty("match mode filter")
    private String matchModeFilter = "contains";

    public McpStudentPagingRequestDto() {
    }

    public McpStudentPagingRequestDto(int page, int rows, String sortBy, String sortDir,
                                     String valueFilter, String matchModeFilter) {
        this.page = page;
        this.rows = rows;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
        this.valueFilter = valueFilter;
        this.matchModeFilter = matchModeFilter;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public String getValueFilter() {
        return valueFilter;
    }

    public void setValueFilter(String valueFilter) {
        this.valueFilter = valueFilter;
    }

    public String getMatchModeFilter() {
        return matchModeFilter;
    }

    public void setMatchModeFilter(String matchModeFilter) {
        this.matchModeFilter = matchModeFilter;
    }

    @Override
    public String toString() {
        return "McpStudentPagingRequestDto{" +
                "page=" + page +
                ", rows=" + rows +
                ", sortBy='" + sortBy + '\'' +
                ", sortDir='" + sortDir + '\'' +
                ", valueFilter='" + valueFilter + '\'' +
                ", matchModeFilter='" + matchModeFilter + '\'' +
                '}';
    }
}
