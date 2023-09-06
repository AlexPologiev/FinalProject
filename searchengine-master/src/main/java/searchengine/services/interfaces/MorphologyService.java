package searchengine.services.interfaces;


import searchengine.dto.statistics.ResponseResult;
import searchengine.model.Page;
import searchengine.model.RootSite;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface MorphologyService {



    void createIndexOfPage(Page page);
    ResponseResult createIndexOfRootSite(RootSite rootSite);
    HashMap<String, Integer> getMapLemmaFrequency(String text);
    Set<String>  getSetLemmaFromQuery(String query);

    List<String> splitTextBySentence(String text);

    List<String> splitTextByUpperWords(String trim);

    String getNormalForm(String word);

    boolean isOfficialPart(String word);
}
