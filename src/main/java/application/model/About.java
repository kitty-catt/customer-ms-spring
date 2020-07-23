package application.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class About {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String parentRepo;

    @Getter
    @Setter
    private String description;

    public About() {

    }

    public About(String name, String parentRepo, String description) {
        this.name = name;
        this.parentRepo = parentRepo;
        this.description = description;
    }

}
