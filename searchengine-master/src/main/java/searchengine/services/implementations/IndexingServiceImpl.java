package searchengine.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.ResponseResult;

import searchengine.model.RootSite;
import searchengine.model.Status;
import searchengine.repositoty.IndexRepository;
import searchengine.repositoty.LemmaRepository;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.RootSiteRepository;
import searchengine.services.interfaces.ChangeEntityService;
import searchengine.services.interfaces.IndexingService;
import searchengine.services.interfaces.MorphologyService;
import searchengine.services.interfaces.UpdatePageService;

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
    private final RootSiteRepository rootSiteRepository;

    private final PageRepository pageRepository;
    private final MorphologyService morphologyService;
    private final ChangeEntityService changeEntityService;



    @Autowired
    public IndexingServiceImpl(SitesList sitesList,
                               JsoupConfig jsoupConfig, RootSiteRepository rootSiteRepository,
                               PageRepository pageRepository,
                               MorphologyService morphologyService,
                               ChangeEntityService changeEntityService) {
        this.sitesList = sitesList;
        this.jsoupConfig = jsoupConfig;
        this.rootSiteRepository = rootSiteRepository;
        this.pageRepository = pageRepository;
        this.morphologyService = morphologyService;
        this.changeEntityService = changeEntityService;
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
           Thread thread = new Thread(() ->createAndIndexRootSite(site));
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
           RootSite rootSite = rootSiteRepository.findByUrl(url);
           if(rootSite != null){
               toFailed(rootSite, "Прервано пользователем");
           }
       }

        return ResponseResult.sendGoodResponse();


    }

    private RootSite createNewRootSiteInDb(Status status, String name, String url) {
        RootSite rootSite = new RootSite(status, LocalDateTime.now(), "", url, name);
        rootSiteRepository.save(rootSite);
        return rootSite;
    }





    private void createAndIndexRootSite(Site site) {
        ForkJoinPool pool = new ForkJoinPool();
        forkJoinPoolList.add(pool);
        String url = site.getUrl();
        String name = site.getName();
        RootSite rootSite = rootSiteRepository.findByUrl(url);
        if(rootSite != null){
            toIndexing(rootSite);
            changeEntityService.deleteAllDataAboutRootSite(rootSite);
        }
        RootSite newRootSite = createNewRootSiteInDb(Status.INDEXING, name, url);

        try {
            pool.invoke(new SiteIndexer(url, pageRepository, morphologyService, newRootSite, true,
                    rootSiteRepository, jsoupConfig));

        } catch (RejectedExecutionException e){
            toFailed(newRootSite, "Прервано пользователем");
        }
        forkJoinPoolList.remove(pool);
        if(newRootSite.getStatus() != Status.FAILED){
            toIndexed(newRootSite);
        }
        //rootSiteRepository.save(newRootSite);




    }

    private void toFailed(RootSite rootSite, String error){
        rootSite.setStatus(Status.FAILED);
        rootSite.setLastErrorText(error);
        rootSiteRepository.save(rootSite);
    }

    private void toIndexed(RootSite rootSite){
        rootSite.setStatus(Status.INDEXED);
        rootSiteRepository.save(rootSite);
    }

    private void toIndexing(RootSite rootSite){
        rootSite.setStatus(Status.INDEXING);
        rootSiteRepository.save(rootSite);
    }



}
