package com.avlweb.collectionviewer.model;

import java.util.ArrayList;

public class Item {
    private String name;
    private String description;
    private ArrayList<String> properties;
    private ArrayList<String> images;
    private boolean selected;
    private int positionInSelectedList;

    public Item() {
        this.selected = true;
        this.positionInSelectedList = -1;
        this.properties = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSelected() {
        this.selected = true;
    }

    public void setNotSelected() {
        this.selected = false;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void addProperty(String property) {
        this.properties.add(property);
    }

    public int getNbProperties() {
        return this.properties.size();
    }

    public void setProperty(int idx, String value) {
        if (idx >= this.properties.size())
            this.properties.add(value);
        else
            this.properties.set(idx, value);
    }

    public String getProperty(int num) {
        if ((num >= 0) && (num < this.properties.size()))
            return this.properties.get(num);

        return null;
    }

    public void addImagePath(String path) {
        this.images.add(path);
    }

    public String getImagePath(int index) {
        if ((index >= 0) && (index < this.images.size()))
            return this.images.get(index);

        return null;
    }

    public void deleteImage(int index) {
        if ((index >= 0) && (index < this.images.size()))
            this.images.remove(index);
    }

    public int getNbImages() {
        return this.images.size();
    }

    public String toString() {
        return this.name;
    }

    public void setPositionInSelectedList(int positionInSelectedList) {
        this.positionInSelectedList = positionInSelectedList;
    }

    public int getPositionInSelectedList() {
        return this.positionInSelectedList;
    }
}