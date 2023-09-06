package searchengine.repositoty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.RootSite;

import java.util.ArrayList;
import java.util.List;
@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Lemma findByLemmaAndRootSite(String lemma, RootSite rootSite);
    List<Lemma> findByLemma(String lemma);
    int countByRootSite(RootSite rootSite);
    default List<Lemma> findByNameLemma(String nameLemma, RootSite rootSite){
       List<Lemma> lemmaList = new ArrayList<>();
       Lemma lemma;
        if(rootSite == null){
            lemmaList = findByLemma(nameLemma);
        } else {
            lemma = findByLemmaAndRootSite(nameLemma, rootSite);
            if(lemma == null){
                return lemmaList;
            } else {
                lemmaList.add(lemma);
            }
        }
        return lemmaList;
    }
}
