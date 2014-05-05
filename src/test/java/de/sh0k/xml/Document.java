package de.sh0k.xml;

import lombok.Getter;
import lombok.experimental.Builder;

@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
@Builder
@Getter
public class Document {

    private int id;

    private String title, description;

    public Document(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }
}
