package com.avlweb.encycloviewer.model;

import java.util.ArrayList;

public class DbItem {
    private String name;
    private String description;
    private ArrayList<String> fields;
    private ArrayList<String> imagePaths;
    private boolean selected;
    private int positionInSelectedList;

    public DbItem() {
        this.selected = true;
        this.positionInSelectedList = -1;
        this.fields = new ArrayList<>();
        this.imagePaths = new ArrayList<>();
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

    public void addField(String field) {
        this.fields.add(field);
    }

    public int getNbFields() {
        return this.fields.size();
    }

    public String getField(int num) {
        if ((num >= 0) && (num < this.fields.size()))
            return this.fields.get(num);

        return null;
    }

    public void addImagePath(String path) {
        this.imagePaths.add(path);
    }

    public String getImagePath(int index) {
        if ((index >= 0) && (index < this.imagePaths.size()))
            return this.imagePaths.get(index);

        return null;
    }

    public void deleteImage(int index) {
        if ((index >= 0) && (index < this.imagePaths.size()))
            this.imagePaths.remove(index);
    }

    public int getNbImages() {
        return this.imagePaths.size();
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

    public void setField(int idx, String value) {
        if (idx >= this.fields.size())
            this.fields.add(value);
        else
            this.fields.set(idx, value);
    }
}