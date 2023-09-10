package searchengine.services.impl;


import searchengine.config.JsoupConfig;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.SiteEntityRepository;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;

public class SiteIndexer extends RecursiveAction {
    private static final int BAD_RESPONSE = 400;
    private PageRepository pageRepository;
    private MorphologyParser morphologyParser;
    private SiteEntityRepository siteEntityRepository;
    private final JsoupConfig jsoupConfig;

    private SiteEntity siteEntity;
    private String url;
    private boolean isFirstAction;


    public SiteIndexer(String url,
                       PageRepository pageRepository,
                       MorphologyParser morphologyParser,
                       SiteEntity siteEntity,
                       boolean isFirstAction,
                       SiteEntityRepository siteEntityRepository, JsoupConfig jsoupConfig) {

        this.url = url;
        this.pageRepository = pageRepository;
        this.morphologyParser = morphologyParser;
        this.siteEntity = siteEntity;
        this.isFirstAction = isFirstAction;
        this.siteEntityRepository = siteEntityRepository;
        this.jsoupConfig = jsoupConfig;
    }

    @Override
    protected void compute() {

        HtmlParser parser = new HtmlParser(jsoupConfig);
        List<String> listLinks = new ArrayList<>();
        String clippedUrl = getUrlWithOutSiteEntity(url);
        if (!isVisited(clippedUrl, siteEntity) && isNotFailed(siteEntity)) {
            try {
                timeUpdate(siteEntity);
                listLinks = parser.parserLines(url);

                String content = parser.getContent();
                int code = parser.getCodeResponse();


                if (!isVisited(clippedUrl, siteEntity)) {
                    Page newPage = savePage(siteEntity, clippedUrl, code, content);
                    if (code < BAD_RESPONSE) {
                        morphologyParser.createIndexOfPage(newPage);
                    } else if (isFirstAction) {
                        toFailed(siteEntity, "Главная страница не дотсупна");
                    }
                }


                List<SiteIndexer> taskList = createTaskList(listLinks);
                taskList.forEach(SiteIndexer::join);

            } catch (CancellationException | InterruptedException exception) {
                toFailed(siteEntity, "Прервано пользователем");

            } catch (Exception e) {
                if (isFirstAction) {
                    toFailed(siteEntity, "Главная страница не дотсупна");


                }


            }
        }
    }


    private List<SiteIndexer> createTaskList(List<String> list) {
        List<SiteIndexer> taskList = new ArrayList<>();

        for (String str : list) {
            SiteIndexer task = new SiteIndexer(str,
                    pageRepository, morphologyParser, siteEntity, false, siteEntityRepository, jsoupConfig);
            task.fork();
            taskList.add(task);
        }
        return taskList;
    }


    private String getUrlWithOutSiteEntity(String urlPage) {
        String urlSiteEntity = siteEntity.getUrl();
        if (urlPage.equals(urlSiteEntity)) {
            return "/";
        }
        return urlPage.replace(urlSiteEntity, "");

    }

    private boolean isVisited(String path, SiteEntity siteEntity) {
        Page page = pageRepository.findByPathAndSiteEntity(path, siteEntity);
        return page != null;
    }

    private void timeUpdate(SiteEntity siteEntity) {
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntityRepository.save(siteEntity);
    }

    private void toFailed(SiteEntity siteEntity, String error) {
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastErrorText(error);
        siteEntityRepository.save(siteEntity);
    }

    private boolean isNotFailed(SiteEntity siteEntity) {
        Status status = siteEntity.getStatus();
        return status != Status.FAILED;
    }

    private synchronized Page savePage(SiteEntity siteEntity, String path, int code, String content) {

        Page newPage = new Page(siteEntity, path, code, content);
        pageRepository.save(newPage);
        return newPage;
    }

}







