package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;


@Entity
@Data
@ToString(exclude = "indexList")

public class Lemma {

    public Lemma(SiteEntity siteEntity, String lemma, int frequency) {
        this.siteEntity = siteEntity;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    public Lemma() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @JoinColumn(name = "site_id")
    @ManyToOne()
    private SiteEntity siteEntity;

    @Column()
    private String lemma;
    private int frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Index> indexList;


}
