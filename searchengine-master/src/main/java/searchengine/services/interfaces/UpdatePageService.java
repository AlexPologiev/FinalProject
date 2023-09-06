package searchengine.services.interfaces;

import searchengine.dto.statistics.ResponseResult;
import searchengine.model.Page;

import java.io.IOException;

public interface UpdatePageService {
    ResponseResult indexPage(String url) throws IOException, InterruptedException;

}
