package searchengine.services.implementations;


import org.springframework.format.annotation.DateTimeFormat;
import searchengine.config.JsoupConfig;
import searchengine.model.Page;
import searchengine.model.RootSite;
import searchengine.model.Status;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.RootSiteRepository;
import searchengine.services.interfaces.MorphologyService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RecursiveAction;

public class SiteIndexer extends RecursiveAction {
    private static boolean isRootSitePage = true;
    private static final int BAD_RESPONSE = 400;
    private PageRepository pageRepository;
    private MorphologyService morphologyService;
    private RootSiteRepository rootSiteRepository;
    private final JsoupConfig jsoupConfig;

    private RootSite rootSite;
    private String url;
    private boolean isFirstAction;


    public SiteIndexer(String url,
                       PageRepository pageRepository,
                       MorphologyService morphologyService,
                       RootSite rootSite,
                       boolean isFirstAction,
                       RootSiteRepository rootSiteRepository, JsoupConfig jsoupConfig) {

        this.url = url;
        this.pageRepository = pageRepository;
        this.morphologyService = morphologyService;
        this.rootSite = rootSite;
        this.isFirstAction = isFirstAction;
        this.rootSiteRepository = rootSiteRepository;
        this.jsoupConfig = jsoupConfig;
    }
    @Override
    protected void compute() {

        HtmlParser parser = new HtmlParser(jsoupConfig);
        List<String> listLinks = new ArrayList<>();
        String clippedUrl = getUrlWithOutRootSite(url);
        if (!isVisited(clippedUrl, rootSite) && isNotFailed(rootSite)) {
        try {
            timeUpdate(rootSite);
            listLinks = parser.parserLines(url);

            String content = parser.getContent();
            int code = parser.getCodeResponse();


            if (!isVisited(clippedUrl, rootSite)) {
                Page newPage = savePage(rootSite,clippedUrl,code,content);
                if (code < BAD_RESPONSE) {
                    morphologyService.createIndexOfPage(newPage);
                } else if(isFirstAction){
                    toFailed(rootSite,"Главная страница не дотсупна");
                }
            }


            List<SiteIndexer> taskList = createTaskList(listLinks);
            taskList.forEach(SiteIndexer::join);

        } catch(CancellationException | InterruptedException exception ){
            toFailed(rootSite,"Прервано пользователем");

        }
        catch (Exception e) {
            if(isFirstAction){
                toFailed(rootSite,"Главная страница не дотсупна");


            }


        }
        }
    }


    private List<SiteIndexer> createTaskList(List<String> list) {
        List<SiteIndexer> taskList = new ArrayList<>();

        for (String str : list) {
            SiteIndexer task = new SiteIndexer(str,
                    pageRepository, morphologyService,  rootSite, false,rootSiteRepository, jsoupConfig);
            task.fork();
            taskList.add(task);
        }
        return taskList;
    }



    private String getUrlWithOutRootSite(String urlPage) {
        String urlRootSite = rootSite.getUrl();
        if (urlPage.equals(urlRootSite)){
            return  "/";
        }
        return urlPage.replace(urlRootSite,"");

//        String tempUrl = url.concat("/");
//        int startPosition = 0;
//        int count = 0;
//        for (int i = 0; i < tempUrl.length(); i++) {
//            if (tempUrl.charAt(i) == '/') {
//                count++;
//            }
//            if (count == 3) {
//                startPosition = i;
//                break;
//            }
//        }
//        String result = tempUrl.substring(startPosition, url.length());
//        return result.isEmpty() ? "/" : result;
    }

    private boolean isVisited(String path, RootSite rootSite){
        Page page = pageRepository.findByPathAndRootSite(path, rootSite);
        return page != null;
    }

    private void timeUpdate(RootSite rootSite){
        rootSite.setStatusTime(LocalDateTime.now());
        rootSiteRepository.save(rootSite);
    }

    private void toFailed(RootSite rootSite, String error){
        rootSite.setStatus(Status.FAILED);
        rootSite.setLastErrorText(error);
        rootSiteRepository.save(rootSite);
    }

    private boolean isNotFailed(RootSite rootSite){
        Status status = rootSite.getStatus();
        return status != Status.FAILED;
    }

    private synchronized  Page savePage(RootSite rootSite, String path, int code, String content){

        Page newPage = new Page(rootSite, path, code, content);
        pageRepository.save(newPage);
        return newPage;
    }

}







