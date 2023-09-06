package searchengine;

import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;


import searchengine.model.Page;
import searchengine.model.RootSite;
import searchengine.model.Status;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.RootSiteRepository;


@SpringBootTest
public class SpringBootJPAIntegrationTest {

    private RootSiteRepository rootSiteRepository;
    private PageRepository pageRepository;
@Autowired
    public SpringBootJPAIntegrationTest(RootSiteRepository rootSiteRepository, PageRepository pageRepository) {
        this.rootSiteRepository = rootSiteRepository;
        this.pageRepository = pageRepository;
    }
    RootSite site1 = new RootSite(
            Status.INDEXED, null,"error","https","my");
    Page page1 = new Page(site1,"httpsPage1",200,"page1");

    @Test
    public void getSitesFromConfig(){
        pageRepository.deleteAll();
        rootSiteRepository.deleteAll();;
        rootSiteRepository.save(site1);
       pageRepository.save(page1);

    }



}
//    RootSite site2 = new RootSite(
//            Status.INDEXED, null,"error","https://www.playback.ru","maxim");
//    RootSite site3 = new RootSite(
//            Status.INDEXED, null,"error","https://www.skillbox.ru","my");
//    RootSite site4 = new RootSite(
////    Page page2 = new Page(site2,"httpsPage2",404,"page2");
////    Page page3 = new Page(site3,"httpsPage3",301,"page3");
////    Page page4 = new Page(site4,"httpsPage4",201,"page4");            Status.INDEXED, null,"error","https://www.lenta.ru","my");