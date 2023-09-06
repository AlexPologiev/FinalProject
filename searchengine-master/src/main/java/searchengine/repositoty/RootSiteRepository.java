package searchengine.repositoty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.RootSite;

import java.util.List;
@Repository
public interface RootSiteRepository extends JpaRepository<RootSite,Integer> {



    RootSite findByUrl(String url);
    RootSite findByUrlAndName(String url,String name);
}
