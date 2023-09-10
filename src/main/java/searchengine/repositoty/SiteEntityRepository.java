package searchengine.repositoty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

@Repository
public interface SiteEntityRepository extends JpaRepository<SiteEntity, Integer> {


    SiteEntity findByUrl(String url);

    SiteEntity findByUrlAndName(String url, String name);
}
