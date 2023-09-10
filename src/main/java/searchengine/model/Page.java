package searchengine.model;


import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import javax.persistence.Index;
import java.util.List;

@Entity
@Table(indexes = @Index(columnList = "path"))
@Data
@ToString(exclude = "indexList")

public class Page {
    public Page() {
    }

    public Page(SiteEntity siteEntity, String path, int code, String content) {
        this.siteEntity = siteEntity;
        this.path = path;
        this.code = code;
        this.content = content;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @JoinColumn(name = "site_id")
    @ManyToOne()
    private SiteEntity siteEntity;
    @Column()
    private String path;
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    private List<searchengine.model.Index> indexList;

}
