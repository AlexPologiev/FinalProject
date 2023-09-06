package searchengine.services.interfaces;

import searchengine.model.Page;
import searchengine.model.RootSite;

public interface ChangeEntityService {

    void deleteIndexAndLemmasByPage(Page page);
    void deleteAllDataAboutRootSite(RootSite rootSite);
}
