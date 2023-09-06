package searchengine.repositoty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.RootSite;



import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    List<Page> findByRootSite(RootSite rootSite);



    Page findByPathAndRootSite(String path,RootSite rootSite);

    int countByRootSite(RootSite rootSite);

    @Query(value = "select p.id,p.code,p.content,p.site_id,p.path from page p " +
            "join `index` i on i.page_id = p.id " +
            "join lemma l on i.lemma_id = l.id " +
            "where l.lemma = :d and l.site_id in(:list) ",
            nativeQuery = true)
    List<Page> findAllPageByLemmaQuery(String d, List<Integer> list);

    @Query(value = "select p.id,p.code,p.content,p.site_id,p.path from page p " +
            "join `index` i on i.page_id = p.id " +
            "join lemma l on i.lemma_id = l.id " +
            "where l.lemma like ?1 and l.site_id = ?2",
            nativeQuery = true)
    List<Page> findAllPageByLemmaAndRootSiteQuery(String lemma, int id);


//    @Query(value = "select p.id,p.code,p.content,p.site_id,p.path, sum(i.rank) as rel from page p " +
//            "join `index` i on i.page_id = p.id " +
//            "join lemma l on i.lemma_id = l.id " +
//            "where l.lemma in(:list) " +
//            "group by p.id " +
//            "having count(*) = :d",
//            nativeQuery = true)

   // List<Page> findAllPageByLemmaAndRootSiteQuery1(List<String> list, int d);


}
