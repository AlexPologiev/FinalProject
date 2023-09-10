package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult {

    private boolean result;
    private String error;

    public static ResponseResult sendGoodResponse() {
        return new ResponseResult(true, "");
    }

    public static ResponseResult sendBadResponse(String error) {
        return new ResponseResult(false, error);
    }
}
