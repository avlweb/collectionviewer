package com.avlweb.encycloviewer;

import java.util.ArrayList;

public class DatabaseInfos {
    private String name;
    private String description;
    private String version;
    private ArrayList<FieldDescription> fieldDescriptions;

    public DatabaseInfos() {
        fieldDescriptions = new ArrayList<>();
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

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public ArrayList<FieldDescription> getFieldDescriptions() {
        return this.fieldDescriptions;
    }

    public int getNbFields() {
        if (this.fieldDescriptions != null) {
            return this.fieldDescriptions.size();
        }
        return 0;
    }

    public String getFieldName(int num) {
        if (this.fieldDescriptions != null) {
            if ((num >= 0) && (num < this.fieldDescriptions.size()))
                return this.fieldDescriptions.get(num).getName();
            else
                return this.fieldDescriptions.get(0).getName();
        }
        return null;
    }

    public void addFieldDescription(FieldDescription field) {
        this.fieldDescriptions.add(field);
    }
}
