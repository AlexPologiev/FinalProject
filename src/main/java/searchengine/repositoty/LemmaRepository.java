package searchengine.repositoty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Lemma findByLemmaAndSiteEntity(String lemma, SiteEntity siteEntity);

    List<Lemma> findByLemma(String lemma);

    int countBySiteEntity(SiteEntity siteEntity);

    default List<Lemma> findByNameLemma(String nameLemma, SiteEntity siteEntity) {
        List<Lemma> lemmaList = new ArrayList<>();
        Lemma lemma;
        if (siteEntity == null) {
            lemmaList = findByLemma(nameLemma);
        } else {
            lemma = findByLemmaAndSiteEntity(nameLemma, siteEntity);
            if (lemma == null) {
                return lemmaList;
            } else {
                lemmaList.add(lemma);
            }
        }
        return lemmaList;
    }
}
