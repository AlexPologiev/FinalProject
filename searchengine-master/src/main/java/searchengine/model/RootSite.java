package searchengine.model;


import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import searchengine.converter.StatusConverter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "site")
public class RootSite {
    public RootSite() {
    }
    public RootSite(Status status, LocalDateTime statusTime, String lastErrorText, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastErrorText = lastErrorText;
        this.url = url;
        this.name = name;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Convert(converter = StatusConverter.class)
    private Status status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statusTime;
    private String lastErrorText;
    private String url;
    private String name;


    @OneToMany(mappedBy = "rootSite",cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Page> pages;

    @OneToMany(mappedBy = "rootSite",cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Lemma> lemmas;

    @Override
    public String toString() {
        return "RootSite{" +
                "id=" + id +
                ", status=" + status +
                ", statusTime=" + statusTime +
                ", lastErrorText='" + lastErrorText + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
