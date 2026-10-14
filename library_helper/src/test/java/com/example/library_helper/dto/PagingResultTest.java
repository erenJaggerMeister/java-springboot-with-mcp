package com.example.library_helper.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagingResultTest {

    @Test
    void testPagingResultFromSpringPage() {
        List<String> items = Arrays.asList("Alpha", "Beta", "Gamma");
        Page<String> page = new PageImpl<>(items, PageRequest.of(0, 3), 10);

        PagingResult<String> result = PagingResult.from(page);

        assertEquals(3, result.getData().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(3, result.getPageSize());
        assertEquals(10L, result.getTotalElements());
        assertEquals(4, result.getTotalPages());
        assertTrue(result.isFirst());
        assertFalse(result.isLast());
        assertTrue(result.isHasNext());
        assertFalse(result.isHasPrevious());
    }

    @Test
    void testPagingResultWithMappedList() {
        List<Integer> entityIds = Arrays.asList(1, 2);
        Page<Integer> page = new PageImpl<>(entityIds, PageRequest.of(1, 2), 4);
        List<String> dtoList = Arrays.asList("DTO-1", "DTO-2");

        PagingResult<String> result = PagingResult.from(page, dtoList);

        assertEquals(2, result.getData().size());
        assertEquals("DTO-1", result.getData().get(0));
        assertEquals(1, result.getPageNumber());
        assertEquals(2, result.getPageSize());
        assertEquals(4L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.isHasNext());
        assertTrue(result.isHasPrevious());
    }
}
