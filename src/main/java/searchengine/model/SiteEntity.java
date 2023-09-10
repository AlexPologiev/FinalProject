package searchengine.model;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "site")
public class SiteEntity {
    public SiteEntity() {
    }

    public SiteEntity(Status status, LocalDateTime statusTime, String lastErrorText, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastErrorText = lastErrorText;
        this.url = url;
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    @Enumerated(EnumType.STRING)
    private Status status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statusTime;
    private String lastErrorText;
    private String url;
    private String name;


    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Page> pages;

    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Lemma> lemmas;

    @Override
    public String toString() {
        return "SiteEntity{" +
                "id=" + id +
                ", status=" + status +
                ", statusTime=" + statusTime +
                ", lastErrorText='" + lastErrorText + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
