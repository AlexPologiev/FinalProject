package searchengine.services.implementations;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.RootSite;
import searchengine.repositoty.IndexRepository;
import searchengine.repositoty.LemmaRepository;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.RootSiteRepository;
import searchengine.services.interfaces.ChangeEntityService;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChangeEntityServiceImpl implements ChangeEntityService {

    private final RootSiteRepository rootSiteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Autowired
    public ChangeEntityServiceImpl(RootSiteRepository rootSiteRepository,
                                   PageRepository pageRepository,
                                   LemmaRepository lemmaRepository,
                                   IndexRepository indexRepository) {
        this.rootSiteRepository = rootSiteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    @Override
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

    @Override
    public void deleteAllDataAboutRootSite(RootSite rootSite) {
        rootSiteRepository.delete(rootSite);

    }

}
