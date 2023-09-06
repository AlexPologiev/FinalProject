package searchengine.services.implementations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
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