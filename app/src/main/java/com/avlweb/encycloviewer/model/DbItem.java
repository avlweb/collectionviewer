package com.avlweb.encycloviewer.model;

import java.util.ArrayList;

public class DbItem {
    private String name;
    private ArrayList<String> fields;
    private ArrayList<String> imagePaths;
    private boolean selected;
    private int listPosition;

    public DbItem() {
        this.selected = true;
        this.listPosition = -1;
        this.fields = new ArrayList<>();
        this.imagePaths = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getImagePath(int num) {
        if ((num >= 0) && (num < this.imagePaths.size()))
            return this.imagePaths.get(num);

        return null;
    }

    public int getNbImages() {
        return this.imagePaths.size();
    }

    public String toString() {
        return this.name;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public int getListPosition() {
        return this.listPosition;
    }

    public void setField(int idx, String value) {
        if (idx >= this.fields.size())
            this.fields.add(value);
        else
            this.fields.set(idx, value);
    }
}