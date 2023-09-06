package searchengine.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "`Index`")
public class Index {
    public Index(Page page, Lemma lemma, int rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JoinColumn(name = "page_id")
    @ManyToOne()
    private Page page;

    @JoinColumn(name = "lemma_id")
    @ManyToOne()
    private Lemma lemma;

    @Column(name = "`rank`",columnDefinition = "FLOAT")
    private int rank;
}
