package com.avlweb.encycloviewer;

import java.util.ArrayList;

public class DbItem {
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;
    private ArrayList<String> imagePaths;
    private boolean selected;
    private int listPosition;

    public DbItem() {
        this.selected = true;
        this.field1 = null;
        this.field2 = null;
        this.field3 = null;
        this.field4 = null;
        this.field5 = null;
        this.imagePaths = null;
        this.listPosition = -1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField1() {
        return this.field1;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField2() {
        return this.field2;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public String getField3() {
        return this.field3;
    }

    public void setField4(String field4) {
        this.field4 = field4;
    }

    public String getField4() {
        return this.field4;
    }

    public void setField5(String field5) {
        this.field5 = field5;
    }

    public String getField5() {
        return this.field5;
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

    public void addImagePath(String path) {
        if (this.imagePaths == null)
            this.imagePaths = new ArrayList<>();

        this.imagePaths.add(path);
    }

    public String getImagePath(int num) {
        if (this.imagePaths != null) {
            if ((num > 0) && (num <= this.imagePaths.size()))
                return this.imagePaths.get(num - 1);
            else
                return this.imagePaths.get(0);
        }

        return null;
    }

    public void releaseImagePath() {
        if (this.imagePaths != null) {
            this.imagePaths.clear();
            this.imagePaths = null;
        }
    }

    public int getLastImageIndex() {
        if (this.imagePaths != null)
            return (this.imagePaths.size());

        return 0;
    }

    public String toString() {
        //return( " " + this.Field1 + " - " + this.index);
        return this.field1;
    }

    public void delImage(int index) {
        if ((this.imagePaths != null) && (index < this.imagePaths.size()))
            this.imagePaths.remove(index);
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    public int getListPosition() {
        return listPosition;
    }
}