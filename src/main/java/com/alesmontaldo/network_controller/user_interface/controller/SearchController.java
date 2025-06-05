package com.alesmontaldo.network_controller.user_interface.controller;

import com.alesmontaldo.network_controller.application.SearchService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @QueryMapping
    public List<Object> search(@Argument String text) {
        return searchService.searchByText(text);
    }
}
