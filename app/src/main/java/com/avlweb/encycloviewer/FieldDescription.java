package com.avlweb.encycloviewer;

public class FieldDescription {
    private int id;
    private String name;
    private String description;

    public FieldDescription() {
        this.name = null;
        this.description = null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
