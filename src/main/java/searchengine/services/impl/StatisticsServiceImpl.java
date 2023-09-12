package searchengine.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositoty.LemmaRepository;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.SiteEntityRepository;
import searchengine.services.StatisticsService;

import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;


@Service

public class StatisticsServiceImpl implements StatisticsService {

    private final SiteEntityRepository siteEntityRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SitesList sites;

    @Autowired
    public StatisticsServiceImpl(SiteEntityRepository siteEntityRepository,
                                 PageRepository pageRepository,
                                 LemmaRepository lemmaRepository,
                                 SitesList sites) {
        this.siteEntityRepository = siteEntityRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.sites = sites;
    }

    @Override
    public StatisticsResponse getStatistics() {
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            DetailedStatisticsItem detailedStatisticsItem = getDetailedStatItem(site);
            detailed.add(detailedStatisticsItem);
        }
        TotalStatistics total = getTotalStatistics(detailed);
        StatisticsData statisticsData = new StatisticsData(total, detailed);
        return new StatisticsResponse(true, statisticsData);
    }

    private TotalStatistics getTotalStatistics(List<DetailedStatisticsItem> listDetailedStatItems) {
        int countSites = listDetailedStatItems.size();
        int countPages = 0;
        int countLemmas = 0;
        boolean indexing = true;

        for (DetailedStatisticsItem statItem : listDetailedStatItems) {
            countPages = countPages + statItem.getPages();
            countLemmas = countLemmas + statItem.getLemmas();
            String status = statItem.getStatus();
            if(status.equals("INDEXING")){
                indexing = false;
            }
        }
        return new TotalStatistics(countSites, countPages, countLemmas, indexing);
    }

    private DetailedStatisticsItem getDetailedStatItem(Site site) {
        String url = site.getUrl();
        String name = site.getName();
        String status;
        long statusTime;
        String error;
        int pages;
        int lemmas;
        SiteEntity siteEntity = siteEntityRepository.findByUrlAndName(url, name);
        if (siteEntity != null) {
            status = siteEntity.getStatus().toString();
            error = siteEntity.getLastErrorText();
            statusTime = siteEntity.getStatusTime().getLong(ChronoField.EPOCH_DAY);
            pages = pageRepository.countBySiteEntity(siteEntity);
            lemmas = lemmaRepository.countBySiteEntity(siteEntity);
        } else {
            status = Status.FAILED.toString();
            error = "Нет данных индексации";
            statusTime = 0;
            pages = 0;
            lemmas = 0;
        }
        return new DetailedStatisticsItem(url, name, status, statusTime, error, pages, lemmas);
    }

}
