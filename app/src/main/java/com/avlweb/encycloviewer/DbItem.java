package com.avlweb.encycloviewer;

import java.util.ArrayList;

public class DbItem {
    private ArrayList<String> fields;
    private ArrayList<String> imagePaths;
    private boolean selected;
    private int listPosition;

    public DbItem() {
        this.selected = true;
        this.imagePaths = null;
        this.listPosition = -1;
        this.fields = new ArrayList<>();
        this.imagePaths = new ArrayList<>();
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

    public String getField(int num) {
        if ((num >= 0) && (num < this.fields.size()))
            return this.fields.get(num);
        else
            return this.fields.get(0);
    }

    public void addImagePath(String path) {
        this.imagePaths.add(path);
    }

    public String getImagePath(int num) {
        if ((num >= 0) && (num < this.imagePaths.size()))
            return this.imagePaths.get(num);
        else
            return this.imagePaths.get(0);
    }

    public int getLastImageIndex() {
        return this.imagePaths.size();
    }

    public String toString() {
        return this.fields.get(0);
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public int getListPosition() {
        return listPosition;
    }
}