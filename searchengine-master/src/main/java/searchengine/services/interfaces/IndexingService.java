package searchengine.services.interfaces;

import searchengine.dto.statistics.ResponseResult;

public interface IndexingService {
    ResponseResult startIndexing();
    ResponseResult stopIndexing();
}
