package searchengine.model;

import lombok.*;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@ToString(exclude = "indexList")
//@Table(name = "Lemma")
//@SQLInsert(sql = "insert into lemma (frequency, lemma, site_id) values (?, ?, ?)" +
//" on duplicate key update frequency = frequency + 1")
//@Table(uniqueConstraints = @UniqueConstraint(name = "f",columnNames = {"lemma","site_id"}))
public class Lemma {

    public Lemma(RootSite rootSite, String lemma, int frequency) {
        this.rootSite = rootSite;
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
    private RootSite rootSite;

    @Column()
    private String lemma;
    private int frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.REMOVE,fetch = FetchType.LAZY)
    private List<Index> indexList;


}
