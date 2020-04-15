package com.avlweb.encycloviewer.model;

import java.util.ArrayList;

public class EncycloDatabase {
    private DatabaseInfos infos;
    private ArrayList<DbItem> itemsList;

    public EncycloDatabase() {
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

    public void setItemsList(ArrayList<DbItem> itemsList) {
        this.itemsList = itemsList;
    }

    public void addItemToList(DbItem item) {
        if (itemsList == null)
            itemsList = new ArrayList<>();
        this.itemsList.add(item);
    }
}
