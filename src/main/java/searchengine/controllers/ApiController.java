package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.ResponseResult;
import searchengine.dto.statistics.SearchRequest;
import searchengine.dto.statistics.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;
import searchengine.services.UpdatePageService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final UpdatePageService updatePageService;
    private final SearchService searchService;

    @Autowired
    public ApiController(StatisticsService statisticsService,
                         IndexingService indexingService,
                         UpdatePageService updatePageService,
                         SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.updatePageService = updatePageService;
        this.searchService = searchService;
    }


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ResponseResult> start() {
        return runAndSendResponse(indexingService.startIndexing());

    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ResponseResult> stop() {
        return runAndSendResponse(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ResponseResult> indexPage(@RequestParam String url) throws IOException, InterruptedException {
        return runAndSendResponse(updatePageService.indexPage(url));


    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String query,
                                                 @RequestParam(required = false) Integer offset,
                                                 @RequestParam(required = false) Integer limit,
                                                 @RequestParam(required = false) String site) {
        SearchRequest searchRequest = new SearchRequest(query, site, offset, limit);
        return ResponseEntity.ok(searchService.search(searchRequest));
    }

    private ResponseEntity<ResponseResult> runAndSendResponse(ResponseResult result) {
        HttpStatus status;
        if (result.isResult()) {
            status = HttpStatus.CREATED;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(result, status);
    }
}
