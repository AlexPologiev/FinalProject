package searchengine.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.JsoupConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.ResponseResult;
import searchengine.model.*;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.SiteEntityRepository;
import searchengine.services.UpdatePageService;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class UpdatePageServiceImpl implements UpdatePageService {

    private final SitesList sitesList;
    private  final SiteEntityRepository siteEntityRepository;
    private final PageRepository pageRepository;
    private final MorphologyParser morphologyParser;
    private final EntityChanger entityChanger;
    private final JsoupConfig jsoupConfig;
    @Autowired
    public UpdatePageServiceImpl(SitesList sitesList,
                                 SiteEntityRepository siteEntityRepository,
                                 PageRepository pageRepository,
                                 MorphologyParser morphologyParser,
                                 EntityChanger entityChanger,
                                 JsoupConfig jsoupConfig) {
        this.sitesList = sitesList;
        this.siteEntityRepository = siteEntityRepository;
        this.pageRepository = pageRepository;
        this.morphologyParser = morphologyParser;
        this.entityChanger = entityChanger;
        this.jsoupConfig = jsoupConfig;
    }

    @Override
    public ResponseResult indexPage(String urlPage) throws IOException, InterruptedException {

        if(!isCorrectSite(urlPage)) {
            return new ResponseResult(false,
                    "Введенный сайт не соотвествует формату: " + "https://...");
        }

        String cutUrlPage = cutSiteEntityFromUrl(urlPage);
        String urlSiteEntity =  separateSiteEntityFormUrl(urlPage);

        SiteEntity siteEntity = siteEntityRepository.findByUrl(urlSiteEntity);


            if(siteEntity != null){
                Status status = siteEntity.getStatus();
                if (status==Status.INDEXING){
                    return ResponseResult.sendBadResponse("Главный сайт в стадии индексации");
                }
                Page page = pageRepository.findByPathAndSiteEntity(cutUrlPage, siteEntity);
                if(page != null){
                    entityChanger.deleteIndexAndLemmasByPage(page);
                }

                Page page1 = createAndSavePage(urlPage, siteEntity);
                morphologyParser.createIndexOfPage(page1);
            } else {
               String name = isExistSiteIntoList(urlSiteEntity);
               if(name != null){
                   SiteEntity siteEntity1 = new SiteEntity(Status.FAILED,LocalDateTime.now(),"",urlSiteEntity,name);
                   siteEntityRepository.save(siteEntity1);
                   Page page1 = createAndSavePage(urlPage, siteEntity1);
                   morphologyParser.createIndexOfPage(page1);
                } else {
                    return new ResponseResult(false,
                            "Данная страница находится за пределами заданных сайтов");
                }
            }



        return new ResponseResult(true,"");
    }

    private String cutSiteEntityFromUrl(String url){
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
    private  String separateSiteEntityFormUrl(String url) {

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

    private String isExistSiteIntoList(String urlSiteEntity){


        for (Site site : sitesList.getSites()){
            String url = site.getUrl();
            if(urlSiteEntity.equals(url)){
                return site.getName();
            }
        }

        return null;
    }



    private boolean isCorrectSite(String urlPage) {
        String regex = "https://.*";
        return urlPage.matches(regex);
    }

    private Page createAndSavePage(String urlPage, SiteEntity siteEntity) throws IOException, InterruptedException {
        HtmlParser parser = new HtmlParser(jsoupConfig);
        int codeResponse = parser.connectAndGetCodeResponse(urlPage);
        Thread.sleep(150);
        String content = parser.connectAndGetContent(urlPage);
        String cutUrlPage = cutSiteEntityFromUrl(urlPage);
        Page page = new Page(siteEntity, cutUrlPage, codeResponse, content);
        pageRepository.save(page);
        return page;
    }
}
