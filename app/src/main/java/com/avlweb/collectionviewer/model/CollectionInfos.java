package com.avlweb.collectionviewer.model;

public class CollectionInfos {
    private String name;
    private String description;
    private String version;
    private String path;
    private String xmlPath;
    private boolean sampleCollection;

    public CollectionInfos() {
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public boolean isSampleCollection() {
        return sampleCollection;
    }

    public void setSampleCollection(boolean sampleCollection) {
        this.sampleCollection = sampleCollection;
    }
}
