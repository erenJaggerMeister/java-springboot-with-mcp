package com.example.library_helper.dto;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Generic pagination result container across MSM microservices.
 *
 * @param <T> Type of the items in the paginated content
 */
public class PagingResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> data = Collections.emptyList();
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrevious;

    public PagingResult() {
    }

    public PagingResult(List<T> data, int pageNumber, int pageSize, long totalElements,
                        int totalPages, boolean isFirst, boolean isLast, boolean hasNext, boolean hasPrevious) {
        this.data = data != null ? data : Collections.emptyList();
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.isFirst = isFirst;
        this.isLast = isLast;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data != null ? data : Collections.emptyList();
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public static class Builder<T> {
        private List<T> data = Collections.emptyList();
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean isFirst;
        private boolean isLast;
        private boolean hasNext;
        private boolean hasPrevious;

        public Builder<T> data(List<T> data) {
            this.data = data;
            return this;
        }

        public Builder<T> pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public Builder<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<T> totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public Builder<T> totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder<T> isFirst(boolean isFirst) {
            this.isFirst = isFirst;
            return this;
        }

        public Builder<T> isLast(boolean isLast) {
            this.isLast = isLast;
            return this;
        }

        public Builder<T> hasNext(boolean hasNext) {
            this.hasNext = hasNext;
            return this;
        }

        public Builder<T> hasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
            return this;
        }

        public PagingResult<T> build() {
            return new PagingResult<>(data, pageNumber, pageSize, totalElements, totalPages, isFirst, isLast, hasNext, hasPrevious);
        }
    }

    public static <T> PagingResult<T> from(Page<T> page) {
        if (page == null) {
            return PagingResult.<T>builder()
                    .data(Collections.emptyList())
                    .pageNumber(0)
                    .pageSize(0)
                    .totalElements(0L)
                    .totalPages(0)
                    .isFirst(true)
                    .isLast(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
        }

        return PagingResult.<T>builder()
                .data(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    public static <E, T> PagingResult<T> from(Page<E> page, List<T> mappedList) {
        if (page == null) {
            return PagingResult.<T>builder()
                    .data(mappedList != null ? mappedList : Collections.emptyList())
                    .pageNumber(0)
                    .pageSize(0)
                    .totalElements(0L)
                    .totalPages(0)
                    .isFirst(true)
                    .isLast(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .build();
        }

        return PagingResult.<T>builder()
                .data(mappedList != null ? mappedList : Collections.emptyList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
