package searchengine.repositoty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Page;

import java.util.List;
@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {

    List<Index> findAllByPage(Page page);



}
