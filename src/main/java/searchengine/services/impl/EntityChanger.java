package searchengine.services.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repositoty.IndexRepository;
import searchengine.repositoty.LemmaRepository;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.SiteEntityRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntityChanger {

    private final SiteEntityRepository siteEntityRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Autowired
    public EntityChanger(SiteEntityRepository siteEntityRepository,
                         PageRepository pageRepository,
                         LemmaRepository lemmaRepository,
                         IndexRepository indexRepository) {
        this.siteEntityRepository = siteEntityRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    public void deleteIndexAndLemmasByPage(Page page) {
        List<Index> listIndex = indexRepository.findAllByPage(page);
        List<Lemma> listLemma = new ArrayList<>();

        for (Index index : listIndex) {
            Lemma lemma = index.getLemma();
            listLemma.add(lemma);
        }
        pageRepository.delete(page);
        for (Lemma lemma : listLemma) {
            int frequency = lemma.getFrequency();
            if (frequency > 1) {
                frequency--;
                lemma.setFrequency(frequency);
                lemmaRepository.save(lemma);
            } else {
                lemmaRepository.delete(lemma);
            }
        }

    }

    public void deleteAllDataAboutSiteEntity(SiteEntity siteEntity) {
        siteEntityRepository.delete(siteEntity);
    }

}
