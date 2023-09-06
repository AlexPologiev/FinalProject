package searchengine.dto.statistics;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
@Data
public class SearchResponse {
    int count;
    boolean result;
    String error = "";
   private List<SearchResult> data = new ArrayList<>();

    @Override
    public String toString() {
        return "SearchResponse{" +
                "count=" + count +
                ", data=" +
                '}';
    }
    public void addSearchResult(SearchResult searchResult){
        data.add(searchResult);
    }

    public  void  sort(){
        data.sort((o1, o2) ->
        {
            if(Float.compare(o2.getRelevance(),o1.getRelevance()) != 0){
                return Float.compare(o2.getRelevance(),o1.getRelevance());
            } else {
                return o2.getUri().compareTo(o1.getUri());
            }
        });
    }

    public SearchResponse show(int offset, int limit){
        SearchResponse searchResponse = new SearchResponse();
        List<SearchResult> searchResults = new ArrayList<>();
        int endIndex = Math.min((offset + limit), this.data.size());
        searchResponse.setResult(this.result);
        searchResponse.setError(this.error);
        searchResponse.setCount(this.count);
        searchResults = this.data.subList(offset, endIndex);
        searchResponse.data.addAll(searchResults);

        return searchResponse;
    }
    public void clear(){
        data.clear();
        count = 0;
        error = "";

    }
}
