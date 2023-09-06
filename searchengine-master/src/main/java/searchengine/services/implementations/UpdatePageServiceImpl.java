package searchengine.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.ResponseResult;
import searchengine.model.*;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.RootSiteRepository;
import searchengine.services.interfaces.ChangeEntityService;
import searchengine.services.interfaces.MorphologyService;
import searchengine.services.interfaces.UpdatePageService;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class UpdatePageServiceImpl implements UpdatePageService {

    private final SitesList sitesList;
    private  final RootSiteRepository rootSiteRepository;
    private final PageRepository pageRepository;
    private final MorphologyService morphologyService;
    private final ChangeEntityService changeEntityService;
    private final JsoupConfig jsoupConfig;
    @Autowired
    public UpdatePageServiceImpl(SitesList sitesList,
                                 RootSiteRepository rootSiteRepository,
                                 PageRepository pageRepository,
                                 MorphologyService morphologyService,
                                 ChangeEntityService changeEntityService, JsoupConfig jsoupConfig) {
        this.sitesList = sitesList;
        this.rootSiteRepository = rootSiteRepository;
        this.pageRepository = pageRepository;
        this.morphologyService = morphologyService;
        this.changeEntityService = changeEntityService;
        this.jsoupConfig = jsoupConfig;
    }

    @Override
    public ResponseResult indexPage(String urlPage) throws IOException, InterruptedException {

        if(!isCorrectSite(urlPage)) {
            return new ResponseResult(false,
                    "Введенный сайт не соотвествует формату: " + "https://...");
        }

        String cutUrlPage = cutRootSiteFromUrl(urlPage);
        String urlRootSite =  separateRootSiteFormUrl(urlPage);

        RootSite rootSite = rootSiteRepository.findByUrl(urlRootSite);


            if(rootSite != null){
                Status status = rootSite.getStatus();
                if (status==Status.INDEXING){
                    return ResponseResult.sendBadResponse("Главный сайт в стадии индексации");
                }
                Page page = pageRepository.findByPathAndRootSite(cutUrlPage,rootSite);
                if(page != null){
                    changeEntityService.deleteIndexAndLemmasByPage(page);
                }

                Page page1 = createAndSavePage(urlPage,rootSite);
                morphologyService.createIndexOfPage(page1);
            } else {
               String name = isExistSiteIntoList(urlRootSite);
               if(name != null){
                   RootSite rootSite1 = new RootSite(Status.FAILED,LocalDateTime.now(),"",urlRootSite,name);
                   rootSiteRepository.save(rootSite1);
                   Page page1 = createAndSavePage(urlPage,rootSite1);
                   morphologyService.createIndexOfPage(page1);
                } else {
                    return new ResponseResult(false,
                            "Данная страница находится за пределами заданных сайтов");
                }
            }



        return new ResponseResult(true,"");
    }

    private String cutRootSiteFromUrl(String url){
        String tempUrl = url.concat("/");
        int startPosition = 0;
        int count = 0;
        for(int i = 0; i < tempUrl.length(); i++){
            if(tempUrl.charAt(i) == '/'){
                count++;
            }
            if(count == 3){
                startPosition = i;
                break;
            }
        }
        String result = tempUrl.substring(startPosition,url.length());
        return result.isEmpty() ? "/" : result;
    }
    private  String separateRootSiteFormUrl(String url) {

        String tempUrl = url.concat("/");
        int endPosition = 0;
        int count = 0;
        for(int i = 0; i < tempUrl.length(); i++){
            if(tempUrl.charAt(i) == '/'){
                count++;
            }
            if(count == 3){
                endPosition = i;
                break;
            }
        }

        return tempUrl.substring(0,endPosition);
    }

    private String isExistSiteIntoList(String urlRootSite){


        for (Site site : sitesList.getSites()){
            String url = site.getUrl();
            if(urlRootSite.equals(url)){
                return site.getName();
            }
        }

        return null;
    }



    private boolean isCorrectSite(String urlPage) {
        String regex = "https://.*";
        return urlPage.matches(regex);
    }

    private Page createAndSavePage(String urlPage, RootSite rootSite) throws IOException, InterruptedException {
        HtmlParser parser = new HtmlParser(jsoupConfig);
        int codeResponse = parser.connectAndGetCodeResponse(urlPage);
        Thread.sleep(150);
        String content = parser.connectAndGetContent(urlPage);
        String cutUrlPage = cutRootSiteFromUrl(urlPage);
        Page page = new Page(rootSite, cutUrlPage, codeResponse, content);
        pageRepository.save(page);
        return page;
    }
}
