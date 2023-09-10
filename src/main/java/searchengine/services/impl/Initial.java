package searchengine.services.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositoty.*;

import java.util.List;

@Component
public class Initial implements CommandLineRunner {

    private final SiteEntityRepository siteEntityRepository;

    private final SitesList sitesList;


    @Autowired
    public Initial(SiteEntityRepository siteEntityRepository,
                   SitesList sitesList) {
        this.siteEntityRepository = siteEntityRepository;
        this.sitesList = sitesList;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Site> sites = sitesList.getSites();
        for (Site site : sites) {
            String url = site.getUrl();
            SiteEntity siteEntity = siteEntityRepository.findByUrl(url);
            if (siteEntity != null) {
                Status status = siteEntity.getStatus();
                {
                    if (status == Status.INDEXING) {
                        siteEntity.setStatus(Status.FAILED);
                        siteEntity.setLastErrorText("Индексация была прервана");
                        siteEntityRepository.save(siteEntity);
                    }
                }
            }
        }


    }

}
