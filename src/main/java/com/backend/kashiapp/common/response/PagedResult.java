package com.backend.kashiapp.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagedResult<T> {

    private List<T> content;
    private int page;
    private int totalPages;
    private long totalElements;
}
