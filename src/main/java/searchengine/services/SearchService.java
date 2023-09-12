package searchengine.services;

import searchengine.dto.statistics.SearchRequest;
import searchengine.dto.statistics.SearchResponse;

import java.util.HashMap;

public interface SearchService {
    SearchResponse search(SearchRequest searchRequest);

}
