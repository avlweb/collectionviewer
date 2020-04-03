package com.avlweb.encycloviewer;

import java.util.ArrayList;

public class DatabaseInfos {
    private String name;
    private String description;
    private String version;
    private ArrayList<String> fieldNames;

    public DatabaseInfos() {
        fieldNames = new ArrayList<>();
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

    public String getFieldName(int num) {
        if (this.fieldNames != null) {
            if ((num > 0) && (num <= this.fieldNames.size()))
                return this.fieldNames.get(num - 1);
            else
                return this.fieldNames.get(0);
        }
        return null;
    }

    public void addFieldName(String name) {
        this.fieldNames.add(name);
    }
}
