package searchengine.services.implementations;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.RootSite;
import searchengine.model.Status;
import searchengine.repositoty.*;

import java.util.List;

@Component
public class Initial implements CommandLineRunner {

    private RootSiteRepository rootSiteRepository;
    private PageRepository pageRepository;
    private  MorphologyServiceImpl morphologyService;
    private IndexRepository indexRepository;
    private LemmaRepository lemmaRepository;
    private final UpdatePageServiceImpl updatePageService;
    private final ChangeEntityServiceImpl changeEntityService;

    private final SitesList sitesList;



    @Autowired
    public Initial(RootSiteRepository rootSiteRepository,
                   PageRepository pageRepository,
                   MorphologyServiceImpl morphologyService,
                   IndexRepository indexRepository,
                   LemmaRepository lemmaRepository,
                   UpdatePageServiceImpl updatePageService,
                   ChangeEntityServiceImpl changeEntityService,
                   SitesList sitesList) {
        this.rootSiteRepository = rootSiteRepository;
        this.pageRepository = pageRepository;
        this.morphologyService = morphologyService;
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        this.updatePageService = updatePageService;
        this.changeEntityService = changeEntityService;
        this.sitesList = sitesList;
    }

    @Override
    public void run(String... args) throws Exception {
    //rootSiteRepository.deleteAll();
    List<Site> sites = sitesList.getSites();
    for (Site site : sites){
        String url = site.getUrl();
        RootSite rootSite = rootSiteRepository.findByUrl(url);
        if(rootSite != null){
            Status status = rootSite.getStatus();{
                if (status == Status.INDEXING){
                    rootSite.setStatus(Status.FAILED);
                    rootSite.setLastErrorText("Индексация была прервана");
                    rootSiteRepository.save(rootSite);
                }
            }
        }
    }


    }

}
