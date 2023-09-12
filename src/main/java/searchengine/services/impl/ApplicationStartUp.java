package searchengine.services.impl;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.services.IndexingService;

import java.util.List;
@Component
public class ApplicationStartUp implements ApplicationListener<ContextRefreshedEvent> {

    private final IndexingService indexingService;

    @Autowired
    public ApplicationStartUp(IndexingService indexingService) {
        this.indexingService = indexingService;
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        indexingService.initSitesInTable();
    }
}
