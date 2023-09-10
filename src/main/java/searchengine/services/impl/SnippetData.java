package searchengine.services.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public    class SnippetData {
    String sentence;
    List<Integer> masPosition;
    int rank;
    int length;


}