package com.avlweb.collectionviewer.model;

import java.util.ArrayList;

public class Collection {

    private CollectionInfos infos;
    private ArrayList<Item> items;
    private ArrayList<Property> properties;

    private static final Collection instance = new Collection();

    public Collection() {
    }

    public static Collection getInstance() {
        return instance;
    }

    public CollectionInfos getInfos() {
        return infos;
    }

    public void setInfos(CollectionInfos infos) {
        this.infos = infos;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public int getNbItems() {
        if (this.items != null)
            return this.items.size();
        else
            return 0;
    }

    public Item getItem(int position) {
        if (this.items != null) {
            if ((position >= 0) && (position < this.items.size()))
                return this.items.get(position);
        }
        return null;
    }

    public void addItem(Item item) {
        if (this.items == null)
            this.items = new ArrayList<>();
        this.items.add(item);
    }

    public ArrayList<Property> getProperties() {
        return this.properties;
    }

    public int getNbProperties() {
        if (this.properties != null)
            return this.properties.size();
        else
            return 0;
    }

    public String getPropertyName(int num) {
        if (this.properties != null) {
            if ((num >= 0) && (num < this.properties.size()))
                return this.properties.get(num).getName();
            else
                return this.properties.get(0).getName();
        }
        return null;
    }

    public void addProperty(Property property) {
        if (this.properties == null)
            this.properties = new ArrayList<>();
        this.properties.add(property);
    }

    public void clear() {
        if (this.items != null) {
            this.items.clear();
            this.items = null;
        }
        if (this.properties != null) {
            this.properties.clear();
            this.properties = null;
        }
    }
}
