package com.avlweb.encycloviewer;

public class DatabaseInfos {
    private String name;
    private String description;
    private String version;
    private String[] fieldNames;

    public DatabaseInfos() {
        fieldNames = new String[5];
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

    public void setFieldName(String name, int index) {
        if (index > 0 && index < 6)
            this.fieldNames[index - 1] = name;
    }

    public String[] getFieldNames() {
        return this.fieldNames;
    }

    public String getFieldName(int index) {
        return this.fieldNames[index - 1];
    }
}
