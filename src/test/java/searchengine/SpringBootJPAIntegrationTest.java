package searchengine;

import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;


import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositoty.PageRepository;
import searchengine.repositoty.SiteEntityRepository;


@SpringBootTest
public class SpringBootJPAIntegrationTest {

    private SiteEntityRepository siteEntityRepository;
    private PageRepository pageRepository;
@Autowired
    public SpringBootJPAIntegrationTest(SiteEntityRepository siteEntityRepository, PageRepository pageRepository) {
        this.siteEntityRepository = siteEntityRepository;
        this.pageRepository = pageRepository;
    }
    SiteEntity site1 = new SiteEntity(
            Status.INDEXED, null,"error","https","my");
    Page page1 = new Page(site1,"httpsPage1",200,"page1");

    @Test
    public void getSitesFromConfig(){
        pageRepository.deleteAll();
        siteEntityRepository.deleteAll();;
        siteEntityRepository.save(site1);
       pageRepository.save(page1);

    }



}
//    SiteEntity site2 = new SiteEntity(
//            Status.INDEXED, null,"error","https://www.playback.ru","maxim");
//    SiteEntity site3 = new SiteEntity(
//            Status.INDEXED, null,"error","https://www.skillbox.ru","my");
//    SiteEntity site4 = new SiteEntity(
////    Page page2 = new Page(site2,"httpsPage2",404,"page2");
////    Page page3 = new Page(site3,"httpsPage3",301,"page3");
////    Page page4 = new Page(site4,"httpsPage4",201,"page4");            Status.INDEXED, null,"error","https://www.lenta.ru","my");