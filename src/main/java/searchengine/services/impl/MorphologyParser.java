package searchengine.services.impl;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.repositoty.IndexRepository;
import searchengine.repositoty.LemmaRepository;
import searchengine.repositoty.PageRepository;


import java.io.IOException;
import java.util.*;

@Component
public class MorphologyParser {

    private static final List<String> particlesNames = List.of("МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ");
    private static final int codeBadResponse = 400;
    LuceneMorphology luceneMorphology = new RussianLuceneMorphology();

    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;


    @Autowired
    public MorphologyParser(
            IndexRepository indexRepository,
            LemmaRepository lemmaRepository
    ) throws IOException {

        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;

    }



    public void createIndexOfPage(Page page) {

        try {
            String htmlText = page.getContent();
            HashMap<String, Integer> mapLemmas = getMapLemmaFrequency(htmlText);
            saveLemmaAndFrequency(mapLemmas, page);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private synchronized void saveLemmaAndFrequency(HashMap<String, Integer> mapLemmas, Page page) {
        SiteEntity siteEntity = page.getSiteEntity();
        List<Lemma> lemmaList = new ArrayList<>();
        List<Index> indexList = new ArrayList<>();
        for (String lem : mapLemmas.keySet()) {
            Lemma lemma = lemmaRepository.findByLemmaAndSiteEntity(lem, siteEntity);

            if (lemma != null) {
                int currentFrequency = lemma.getFrequency();
                lemma.setFrequency(currentFrequency + 1);
            } else {
                lemma = new Lemma(siteEntity, lem, 1);
            }
            lemmaList.add(lemma);
            int rank = mapLemmas.get(lem);
            Index index = new Index(page, lemma, rank);
            indexList.add(index);
        }

        lemmaRepository.saveAll(lemmaList);
        indexRepository.saveAll(indexList);
    }


    public HashMap<String, Integer> getMapLemmaFrequency(String text) {

        HashMap<String, Integer> lemmas = new HashMap<>();
        String textWithoutHtmlTags = getTextWithoutHtmlTags(text);
        List<String> listWords = splitTextByWords(textWithoutHtmlTags);

        for (String word : listWords) {

            if (word.isBlank() || isOfficialPart(word)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);
            if (lemmas.containsKey(normalWord)) {
                Integer currentCount = lemmas.get(normalWord);
                lemmas.put(normalWord, currentCount + 1);
            } else {
                lemmas.put(normalWord, 1);
            }

        }
        return lemmas;
    }


    public Set<String> getSetLemmaFromQuery(String query) {
        Set<String> resultSet = new HashSet<>();
        List<String> listWords = splitTextByWords(query);
        for (String word : listWords) {

            if (word.isBlank() || isOfficialPart(word)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);
            resultSet.add(normalWord);

        }

        return resultSet;
    }


    public List<String> splitTextByWords(String text) {

        String regexPunctuationMarks = "[^а-яА-Я\\s]";
        String regexWhiteSigns = "\\s+";

        String lowCaseText = text.toLowerCase();
        String str = lowCaseText.replaceAll(regexPunctuationMarks, "");
        String[] massiveWords = str.split(regexWhiteSigns);

        return Arrays.stream(massiveWords).toList();
    }

    public List<String> splitTextByUpperWords(String text) {

        String regexPunctuationMarks = "[^а-яА-Я\\s]";
        String regexWhiteSigns = "\\s+";

        String str = text.replaceAll(regexPunctuationMarks, "");
        String[] massiveWords = str.split(regexWhiteSigns);
        return Arrays.stream(massiveWords).toList();
    }


    public List<String> splitTextBySentence(String text) {

        //String regexWhiteSigns = "\\s{2,}";
        String regexWhiteSigns = "[^а-яА-Я\\s0-9,\\-:/\"«»—a-zA-z]";
        String[] massiveWords = text.split(regexWhiteSigns);
        return Arrays.stream(massiveWords).toList();
    }

    public boolean isOfficialPart(String word) {
        boolean result = false;
        String wordLowCase = word.toLowerCase();
        List<String> morphData = luceneMorphology.getMorphInfo(wordLowCase);

        for (String partName : particlesNames) {
            if (morphData.get(0).contains(partName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public String getTextWithoutHtmlTags(String text) {

        String regexForHtmlTags = "<[^>]*>";
        return text.replaceAll(regexForHtmlTags, "");
    }

    public String getNormalForm(String world) {
        String lowerCasWorld = world.toLowerCase();
        List<String> listNormalForms = luceneMorphology.getNormalForms(lowerCasWorld);
        if (listNormalForms.isEmpty()) {
            return " ";
        }
        return listNormalForms.get(0);
    }

}
