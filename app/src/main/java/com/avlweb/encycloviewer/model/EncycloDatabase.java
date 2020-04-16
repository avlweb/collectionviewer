package com.avlweb.encycloviewer.model;

import java.util.ArrayList;

public class EncycloDatabase {

    private DatabaseInfos infos;
    private ArrayList<DbItem> itemsList;
    private ArrayList<FieldDescription> fieldDescriptions;

    private static final EncycloDatabase instance = new EncycloDatabase();

    public EncycloDatabase() {
    }

    public static EncycloDatabase getInstance() {
        return instance;
    }

    public DatabaseInfos getInfos() {
        return infos;
    }

    public void setInfos(DatabaseInfos infos) {
        this.infos = infos;
    }

    public ArrayList<DbItem> getItemsList() {
        return itemsList;
    }

    public void addItemToList(DbItem item) {
        if (this.itemsList == null)
            this.itemsList = new ArrayList<>();
        this.itemsList.add(item);
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
        if (this.fieldDescriptions == null)
            this.fieldDescriptions = new ArrayList<>();
        this.fieldDescriptions.add(field);
    }

    public void clear() {
        if (this.itemsList != null) {
            this.itemsList.clear();
            this.itemsList = null;
        }
        if (this.fieldDescriptions != null) {
            this.fieldDescriptions.clear();
            this.fieldDescriptions = null;
        }
    }
}
