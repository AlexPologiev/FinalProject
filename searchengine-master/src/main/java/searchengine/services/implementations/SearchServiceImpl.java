package searchengine.services.implementations;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.SearchRequest;
import searchengine.dto.statistics.SearchResponse;
import searchengine.dto.statistics.SearchResult;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.RootSite;
import searchengine.repositoty.LemmaRepository;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.RootSiteRepository;
import searchengine.services.interfaces.MorphologyService;
import searchengine.services.interfaces.SearchService;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    static final int OFFSET = 0;
    static final  int LIMIT = 20;
    static final  int LENGTH_OF_SNIPPET = 240;
    static final int CUT_LIMIT = 247;
    static final  int REMAINDER = 50;

    private final MorphologyService morphologyService;
    private final LemmaRepository lemmaRepository;
    private final RootSiteRepository rootSiteRepository;

    private final PageRepository pageRepository;
    private final SitesList sitesList;
    private  SearchResponse searchResponse;


    @Autowired
    public SearchServiceImpl(MorphologyService morphologyService,
                             LemmaRepository lemmaRepository,
                             RootSiteRepository rootSiteRepository,
                             PageRepository pageRepository,
                             SitesList sitesList, SearchResponse searchResponse) {
        this.morphologyService = morphologyService;
        this.lemmaRepository = lemmaRepository;
        this.rootSiteRepository = rootSiteRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;
        this.searchResponse = searchResponse;
    }


    @Override
    public SearchResponse search(SearchRequest searchRequest) {

        int offset = searchRequest.getOffset() == null ? OFFSET : searchRequest.getOffset();
        int limit = searchRequest.getLimit() == null ? LIMIT : searchRequest.getLimit();


        if (offset == 0){
            searchResponse.clear();
            runSearch(searchRequest,searchResponse);
        }

        return searchResponse.show(offset,limit);
    }

    private SearchResponse runSearch(SearchRequest searchRequest, SearchResponse searchResponse) {

        Map<Page,Float> mapPageRelativeRelevance;
        String urlRootSite = searchRequest.getSite();


        HashMap<String, Integer> mapLemmaFrequency = getMapLemmaFrequency(searchRequest);
        if(mapLemmaFrequency.isEmpty()){
            searchResponse.setResult(true);
            return searchResponse;
        }


        RootSite rootSite = rootSiteRepository.findByUrl(urlRootSite);
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(mapLemmaFrequency.entrySet());
        sortedList.sort((o1, o2) -> Integer.compare(o1.getValue(), o2.getValue()));

        List<Page> resultList = getResultSearchList(sortedList,rootSite);

        if (resultList.isEmpty()) {
            searchResponse.setResult(true);
            return searchResponse;
        }
        List<String> lemmaList = new ArrayList<>();

        for (Map.Entry<String,Integer> entry : sortedList){
            lemmaList.add(entry.getKey());
        }

        mapPageRelativeRelevance = createMapPageRelativeRelevance(resultList,lemmaList);

        fillSearchResponse(searchResponse,
                mapPageRelativeRelevance,searchRequest);

        return searchResponse;
    }


    private HashMap<String, Integer> getFrequency(Set<String> setLemmas, RootSite rootSite) {

        HashMap<String, Integer> resultMap = new HashMap<>();
        List<Lemma> lemmaList;
        int resultFrequency = 0;
        for (String nameLemma : setLemmas) {
            lemmaList = lemmaRepository.findByNameLemma(nameLemma,rootSite);
            if (lemmaList.isEmpty()) {
                resultMap.clear();
                return resultMap;
            } else {
                for (Lemma lemma : lemmaList){
                    int frequency = lemma.getFrequency();
                    resultFrequency = resultFrequency + frequency;
                }
            }
            resultMap.put(nameLemma, resultFrequency);
        }

        return resultMap;
    }

    private List<Page> getResultSearchList(List<Map.Entry<String,Integer>> listLemmasAndFrequency,
                                           RootSite rootSite){
        List<Page> resultList = new ArrayList<>();
        List<Lemma> lemmaList;
        List<Integer> listIds = getRootSiteIdsFromConfig(sitesList);
          if(listIds.isEmpty()){
              return resultList;
          }
        for (int i = 0; i < listLemmasAndFrequency.size(); i++) {
            Map.Entry<String, Integer> entry = listLemmasAndFrequency.get(i);
            String stringLemma = entry.getKey();

            if (i == 0) {
                if (rootSite == null) {
                    resultList = pageRepository.findAllPageByLemmaQuery(stringLemma, listIds);
                } else {
                    resultList = pageRepository.findAllPageByLemmaAndRootSiteQuery(stringLemma,
                            rootSite.getId());
                }
            } else {
                lemmaList = lemmaRepository.findByNameLemma(stringLemma,rootSite);

                    findPagesByLemma(lemmaList, resultList);


                if (resultList.isEmpty()) {
                    break;
                }
            }


        }

        return resultList;
    }

    private Map<Page,Float> createMapPageAbsoluteRelevance(List<Page> pageList,
                                                           List<String> lemmaList){
        Map<Page,Float> resultMap = new HashMap<>();
        float relevance = 0;
        for(Page page : pageList){
            List<Index> indexList = page.getIndexList();
            for(String lem : lemmaList){
                Lemma lemma1 = lemmaRepository.findByNameLemma(lem,page.getRootSite()).get(0);
                for(Index index : indexList){
                    Lemma lemma = index.getLemma();
                    if(lemma == lemma1){
                        relevance = relevance + index.getRank();
                        break;
                    }

                }
            }
            resultMap.put(page,relevance);
            relevance = 0;
        }

        return resultMap;
    }

    private Map<Page,Float> createMapPageRelativeRelevance(List<Page> pageList,
                                                           List<String> lemmaList){

        Map<Page,Float> mapPageAbsoluteRelevance = createMapPageAbsoluteRelevance(pageList,lemmaList);
        Map<Page,Float> resultMap = new HashMap<>();

        Collection<Float> listOfRelevance = mapPageAbsoluteRelevance.values();
        Float maxRelevance = Collections.max(listOfRelevance);
        for (Page page : mapPageAbsoluteRelevance.keySet()){
            float relativeRelevance = (float) Math.round(mapPageAbsoluteRelevance.get(page)/maxRelevance*1000)/1000;
            resultMap.put(page,relativeRelevance);
        }

        return resultMap;
    }
    private void findPagesByLemma(List<Lemma> lemmaList, List<Page> pageList) {
        List<Page> resultList = new ArrayList<>(pageList);

        for (Page page : pageList) {
            List<Index> indexList = page.getIndexList();
            boolean match = false;
            for (Index index : indexList) {
                Lemma l = index.getLemma();
                for (Lemma lemma : lemmaList){
                    if (l == lemma) {
                        match = true;
                        break;
                    }
                }
                if (match){
                    break;
                }
            }
            if (!match) {
                resultList.remove(page);
            }
        }
        pageList.clear();
        pageList.addAll(resultList);

    }

    private void fillSearchResponse(SearchResponse searchResponse,
                                    Map<Page,Float> mapPageRelevance,SearchRequest searchRequest) {

        int count=0;
        for(Page page : mapPageRelevance.keySet()){
            String site = page.getRootSite().getUrl();
            String name = page.getRootSite().getName();
            String uri = page.getPath();
            String htmlContent = page.getContent();
            Document document = Jsoup.parse(htmlContent);
            Element elementTitle = document.select("title").first();
            String title;
            if(elementTitle!=null){
                title = elementTitle.text();
            } else {
                title = "";
            }
            float relevance = mapPageRelevance.get(page);
            String query = searchRequest.getQuery();
            String pageText = document.text();
            String snippet = getSnippet(query,pageText);

            SearchResult result = new SearchResult(site,name,uri,title,
                    snippet,relevance);
            count++;

            searchResponse.addSearchResult(result);


        }
        searchResponse.setCount(count);
        searchResponse.setResult(true);
        searchResponse.sort();
    }

    private HashMap<String, Integer> getMapLemmaFrequency(SearchRequest searchRequest){
        HashMap<String, Integer> mapLemmaFrequency = new HashMap<>();
        String query = searchRequest.getQuery();

        Set<String> inputSetLemmas = morphologyService.getMapLemmaFrequency(query).keySet();
        if(!inputSetLemmas.isEmpty()){
            String urlRootSite = searchRequest.getSite();
            RootSite rootSite = rootSiteRepository.findByUrl(urlRootSite);
            mapLemmaFrequency = getFrequency(inputSetLemmas, rootSite);
        }

        return  mapLemmaFrequency;
    }

    public String getSnippet(String query, String pageText){


        Set<String> setLemmas = morphologyService.getSetLemmaFromQuery(query);
        List<String> sentenceList = morphologyService.splitTextBySentence(pageText);
        List<SnippetData> listData = new ArrayList<>();
        for (String sentence : sentenceList){
            int rank = 0;
            int lengthSentence = sentence.length();
            int lastIndex = 0;
            List<Integer> listPosition = new ArrayList<>();
            List<String> words = morphologyService.splitTextByUpperWords(sentence.trim());
            for (String word : words){
                if(word.isBlank()){
                    continue;
                }
                String normalForm = morphologyService.getNormalForm(word);

                if (normalForm.isBlank() || morphologyService.isOfficialPart(word)) {
                    continue;
                }

                for(String lemma : setLemmas){
                    if(normalForm.equals(lemma)){
                        int startPosition = sentence.indexOf(word,lastIndex);
                        if (startPosition < 0){
                            continue;
                        }
                        int endPosition = startPosition + word.length();
                        listPosition.add(startPosition);
                        listPosition.add(endPosition);
                        lastIndex = endPosition;
                        rank++;
                        break;
                    }
                }
            }
            if (rank > 0){
                SnippetData snippetData = new SnippetData(sentence,listPosition,rank,lengthSentence);
                listData.add(snippetData);
            }

        }
        listData.sort((o1, o2) -> Integer.compare(o2.rank,o1.rank));

        return assembleSnippet(listData);
    }

    private String makeSnippetInBold(SnippetData snippetData){
        String sentence = snippetData.getSentence();
        List<Integer> positions = snippetData.getMasPosition();
        StringBuilder stringBuilder = new StringBuilder(sentence);
        int count = 0;
        for (int i = 0; i < positions.size(); i++) {
            String sign;
            if (i % 2 == 0) {
                sign = "<b>";
            } else {
                sign = "</b>";
            }
            stringBuilder.insert(positions.get(i) + count, sign);

            count = count + sign.length();
        }

        return stringBuilder.toString();
    }

    private String assembleSnippet(List<SnippetData> snippetDataList){
        List<SnippetData> bufferList = new ArrayList<>();
        int capacity = 0;
        for(SnippetData data : snippetDataList){
            bufferList.add(data);
            String sentence = data.getSentence();
            int lengthSentence = sentence.length();
            capacity = capacity + lengthSentence;
            int difference = LENGTH_OF_SNIPPET - capacity;
            if(difference < 0){
                return cutSnippet(bufferList);
            }
            if(difference < REMAINDER) {
                break;
            }
        }

        return clueAndMakeInBoldSnippet(bufferList).toString();
    }

    private String cutSnippet(List<SnippetData> snippetDataList){
        StringBuilder resultString;
        int sizeList = snippetDataList.size();
        if(sizeList == 1){
            SnippetData data = snippetDataList.get(0);
            String sentence = data.getSentence();
            String snippet;
            List<Integer> listIndex = data.getMasPosition();
            int startIndex = listIndex.get(0);
            int endIndex = listIndex.get(listIndex.size() - 1);
            int difference = endIndex - startIndex;
            if(endIndex < LENGTH_OF_SNIPPET){
               snippet = sentence.substring(0,LENGTH_OF_SNIPPET);
               data.setSentence(snippet);
                return makeSnippetInBold(data);
            } else if(difference <= LENGTH_OF_SNIPPET){
                snippet = sentence.substring(endIndex-LENGTH_OF_SNIPPET,endIndex);
               List<Integer> buffer = new ArrayList<>(listIndex);
               listIndex.clear();
               for(int index : buffer){
                   listIndex.add(index - (endIndex - LENGTH_OF_SNIPPET));
               }
                data.setSentence(snippet);
                return makeSnippetInBold(data);
            } else {
                endIndex = Math.min(startIndex + LENGTH_OF_SNIPPET, sentence.length());
                snippet = sentence.substring(startIndex,endIndex);
                List<Integer> buffer = new ArrayList<>(listIndex);
                listIndex.clear();
                for(int index : buffer){
                    if (index < endIndex){
                        listIndex.add(index - startIndex);
                    }

                }
                data.setSentence(snippet);
                return makeSnippetInBold(data);
            }

        }

        resultString = clueAndMakeInBoldSnippet(snippetDataList);
        resultString.delete(CUT_LIMIT,resultString.length());
        resultString.append("...");
        return resultString.toString();
    }

    private StringBuilder clueAndMakeInBoldSnippet(List<SnippetData> snippetData){
        StringBuilder result = new StringBuilder();
        for (SnippetData data : snippetData){
            String sentence = makeSnippetInBold(data);
            result.append(sentence);
        }
        result.append("...");
        return result;
    }

    private List<Integer> getRootSiteIdsFromConfig(SitesList sitesList){
        List<Site> sites = sitesList.getSites();
        List<Integer> listIds = new ArrayList<>();
        for(Site site : sites){
            String url = site.getUrl();
            RootSite rootSite = rootSiteRepository.findByUrl(url);
            if(rootSite != null){
                listIds.add(rootSite.getId());
            }
        }

        return listIds;

    }
}










