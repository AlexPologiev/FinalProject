package searchengine.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.ResponseResult;

import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.SiteEntityRepository;
import searchengine.services.IndexingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;

@Service
public class IndexingServiceImpl implements IndexingService {

    List<ForkJoinPool> forkJoinPoolList = new ArrayList<>();
    private final SitesList sitesList;
    private final JsoupConfig jsoupConfig;
    private final SiteEntityRepository siteEntityRepository;

    private final PageRepository pageRepository;
    private final MorphologyParser morphologyParser;
    private final EntityChanger entityChanger;



    @Autowired
    public IndexingServiceImpl(SitesList sitesList,
                               JsoupConfig jsoupConfig, SiteEntityRepository siteEntityRepository,
                               PageRepository pageRepository,
                               MorphologyParser morphologyParser,
                               EntityChanger entityChanger) {
        this.sitesList = sitesList;
        this.jsoupConfig = jsoupConfig;
        this.siteEntityRepository = siteEntityRepository;
        this.pageRepository = pageRepository;
        this.morphologyParser = morphologyParser;
        this.entityChanger = entityChanger;

    }

    @Override
    public ResponseResult startIndexing() {

        Set<Site> siteSet = new HashSet<>(sitesList.getSites());
        if (siteSet.isEmpty()){
            return ResponseResult.sendBadResponse("Конфигурационный файл пуст");
        }

        if (!forkJoinPoolList.isEmpty()){
            return ResponseResult.sendBadResponse("Индексация уже запущена");
        }

        for (Site site : siteSet) {
           Thread thread = new Thread(() -> createAndIndexSiteEntity(site));
           thread.start();
        }

        return ResponseResult.sendGoodResponse();

    }




    @Override
    public ResponseResult stopIndexing() {

        if(forkJoinPoolList.isEmpty()) {
            return ResponseResult.sendBadResponse("Индексация не запущена");
        }
        forkJoinPoolList.forEach(ForkJoinPool::shutdownNow);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (Site site : sitesList.getSites()){
           String url = site.getUrl();
           SiteEntity siteEntity = siteEntityRepository.findByUrl(url);
           if(siteEntity != null){
               toFailed(siteEntity, "Прервано пользователем");
           }
       }

        return ResponseResult.sendGoodResponse();


    }

    private SiteEntity createNewSiteEntityInDb(Status status, String name, String url) {
        SiteEntity siteEntity = new SiteEntity(status, LocalDateTime.now(), "", url, name);
        siteEntityRepository.save(siteEntity);
        return siteEntity;
    }





    private void createAndIndexSiteEntity(Site site) {
        ForkJoinPool pool = new ForkJoinPool();
        forkJoinPoolList.add(pool);
        String url = site.getUrl();
        String name = site.getName();
        SiteEntity siteEntity = siteEntityRepository.findByUrl(url);
        if(siteEntity != null){
            toIndexing(siteEntity);
            entityChanger.deleteAllDataAboutSiteEntity(siteEntity);
        }
        SiteEntity newSiteEntity = createNewSiteEntityInDb(Status.INDEXING, name, url);

        try {
            pool.invoke(new SiteIndexer(url, pageRepository, morphologyParser, newSiteEntity, true,
                    siteEntityRepository, jsoupConfig));

        } catch (RejectedExecutionException e){
            toFailed(newSiteEntity, "Прервано пользователем");
        }
        forkJoinPoolList.remove(pool);
        if(newSiteEntity.getStatus() != Status.FAILED){
            toIndexed(newSiteEntity);
        }





    }

    private void toFailed(SiteEntity siteEntity, String error){
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastErrorText(error);
        siteEntityRepository.save(siteEntity);
    }

    private void toIndexed(SiteEntity siteEntity){
        siteEntity.setStatus(Status.INDEXED);
        siteEntityRepository.save(siteEntity);
    }

    private void toIndexing(SiteEntity siteEntity){
        siteEntity.setStatus(Status.INDEXING);
        siteEntityRepository.save(siteEntity);
    }



}
