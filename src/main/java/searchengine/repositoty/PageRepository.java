package searchengine.repositoty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.SiteEntity;


import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    List<Page> findBySiteEntity(SiteEntity siteEntity);


    Page findByPathAndSiteEntity(String path, SiteEntity siteEntity);

    int countBySiteEntity(SiteEntity siteEntity);

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
    List<Page> findAllPageByLemmaAndSiteEntityQuery(String lemma, int id);


}
