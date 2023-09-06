package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.repository.CrudRepository;

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
    public Page(RootSite rootSite, String path, int code, String content) {
        this.rootSite = rootSite;
        this.path = path;
        this.code = code;
        this.content = content;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @JoinColumn(name = "site_id")
    @ManyToOne()
    private RootSite rootSite;
    @Column()
    private String path;
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    private List<searchengine.model.Index> indexList;

}
