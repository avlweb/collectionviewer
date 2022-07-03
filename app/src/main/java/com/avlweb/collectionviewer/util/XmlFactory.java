package com.avlweb.collectionviewer.util;

import static com.avlweb.collectionviewer.ui.Home.SAMPLE_COLLECTION_EN_XML;
import static com.avlweb.collectionviewer.ui.Home.SAMPLE_COLLECTION_FR_XML;

import android.util.Log;
import android.util.Xml;
import android.view.View;

import com.avlweb.collectionviewer.model.CollectionInfos;
import com.avlweb.collectionviewer.model.CollectionItem;
import com.avlweb.collectionviewer.model.CollectionModel;
import com.avlweb.collectionviewer.model.CollectionProperty;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

public class XmlFactory {
    private static final String ns = null;

    public static void readXMLFile(String path) {
        CollectionModel collectionModel = CollectionModel.getInstance();
        collectionModel.clear();
        FileInputStream fin = null;
        InputStreamReader isr = null;
        CollectionItem item = null;
        CollectionProperty property = null;
        CollectionInfos infos = null;

        XmlPullParser parser = Xml.newPullParser();
        try {
            fin = new FileInputStream(path);
            isr = new InputStreamReader(fin);
            parser.setInput(isr);

            int eventType = parser.getEventType();

            boolean caseItem = false;
            boolean caseItems = false;
            boolean caseContent = false;
            boolean caseProperty = false;
            boolean caseProperties = false;
            int type = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String strNode = parser.getName();
                    if (strNode.equals("content")) {
                        caseContent = true;
                        infos = new CollectionInfos();
                    } else if (strNode.equals("properties")) {
                        caseProperties = true;
                    } else if (caseProperties && (strNode.equals("property"))) {
                        caseProperty = true;
                        property = new CollectionProperty();
                    } else if (strNode.equals("items")) {
                        caseItems = true;
                    } else if (caseItems && (strNode.equals("item"))) {
                        caseItem = true;
                        item = new CollectionItem();
                    } else if (caseContent) {
                        switch (strNode) {
                            case "name":
                                type = 1;
                                break;
                            case "description":
                                type = 2;
                                break;
                            case "version":
                                type = 3;
                                break;
                        }
                    } else if (caseProperty) {
                        switch (strNode) {
                            case "name":
                                type = 1;
                                break;
                            case "description":
                                type = 2;
                                break;
                        }
                    } else if (caseItem) {
                        switch (strNode) {
                            case "property":
                                type = 1;
                                break;
                            case "img":
                                type = 2;
                                break;
                            case "name":
                                type = 3;
                                break;
                            case "description":
                                type = 4;
                                break;
                        }
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (caseContent) {
                        switch (type) {
                            case 1:
                                infos.setName(parser.getText());
                                break;
                            case 2:
                                infos.setDescription(parser.getText());
                                break;
                            case 3:
                                infos.setVersion(parser.getText());
                                break;
                        }
                    } else if (caseProperty) {
                        switch (type) {
                            case 1:
                                property.setName(parser.getText());
                                property.setId(View.generateViewId());
                                break;
                            case 2:
                                property.setDescription(parser.getText());
                                break;
                        }
                    } else if (caseItem) {
                        switch (type) {
                            case 1:
                                item.addProperty(parser.getText());
                                break;
                            case 2:
                                item.addImagePath(parser.getText());
                                break;
                            case 3:
                                item.setName(parser.getText());
                                break;
                            case 4:
                                item.setDescription(parser.getText());
                                break;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String strNode = parser.getName();
                    if (strNode.equals("content")) {
                        if (infos != null) {
                            infos.setXmlPath(path);
                            infos.setPath(new File(path).getParent());
                            infos.setSampleCollection(path.endsWith(SAMPLE_COLLECTION_FR_XML) || path.endsWith(SAMPLE_COLLECTION_EN_XML));
                            collectionModel.setInfos(infos);
                        }
                        caseContent = false;
                    } else if (strNode.equals("properties")) {
                        caseProperties = false;
                    } else if (caseProperties && (strNode.equals("property"))) {
                        collectionModel.addProperty(property);
                        caseProperty = false;
                    } else if (strNode.equals("items")) {
                        caseItems = false;
                    } else if (caseItems && (strNode.equals("item"))) {
                        collectionModel.addItem(item);
                        caseItem = false;
                    }
                    type = 0;
                }

                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String result = JsonFactory.writeJsonFile();
        Log.d("XML_FACTORY", "JSON : " + result);
    }

    public static CollectionInfos readCollectionInfos(String path) {
        FileInputStream fin = null;
        InputStreamReader isr = null;
        CollectionInfos infos = null;

        XmlPullParser parser = Xml.newPullParser();
        try {
            fin = new FileInputStream(path);
            isr = new InputStreamReader(fin);
            parser.setInput(isr);

            int eventType = parser.getEventType();

            boolean caseContent = false;
            int type = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String strNode = parser.getName();
                    if (strNode.equals("content")) {
                        caseContent = true;
                        infos = new CollectionInfos();
                    } else if (caseContent) {
                        switch (strNode) {
                            case "name":
                                type = 1;
                                break;
                            case "description":
                                type = 2;
                                break;
                            case "version":
                                type = 3;
                                break;
                        }
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (caseContent) {
                        switch (type) {
                            case 1:
                                infos.setName(parser.getText());
                                break;
                            case 2:
                                infos.setDescription(parser.getText());
                                break;
                            case 3:
                                infos.setVersion(parser.getText());
                                break;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String strNode = parser.getName();
                    if (strNode.equals("content")) {
                        break;
                    }
                    type = 0;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return infos;
    }

    public static boolean writeXml() {

        CollectionModel collectionModel = CollectionModel.getInstance();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(collectionModel.getInfos().getXmlPath());
            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            StringWriter writer = new StringWriter();

            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(ns, "collection");

            insertInfos(xmlSerializer, collectionModel.getInfos());
            insertProperties(xmlSerializer, collectionModel.getProperties());
            insertItems(xmlSerializer, collectionModel.getItems());

            xmlSerializer.endTag(ns, "collection");
            xmlSerializer.endDocument();
            xmlSerializer.flush();

            String dataWrite = writer.toString();
            fileOutputStream.write(dataWrite.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void insertInfos(XmlSerializer xmlSerializer, CollectionInfos dbInfos) throws IOException {
        xmlSerializer.startTag(ns, "content");

        xmlSerializer.startTag(ns, "name");
        xmlSerializer.text(dbInfos.getName());
        xmlSerializer.endTag(ns, "name");

        xmlSerializer.startTag(ns, "description");
        if (dbInfos.getDescription() != null)
            xmlSerializer.text(dbInfos.getDescription());
        xmlSerializer.endTag(ns, "description");

        xmlSerializer.startTag(ns, "version");
        if (dbInfos.getVersion() != null)
            xmlSerializer.text(dbInfos.getVersion());
        xmlSerializer.endTag(ns, "version");

        xmlSerializer.endTag(ns, "content");
    }

    private static void insertProperties(XmlSerializer xmlSerializer, List<CollectionProperty> properties) throws IOException {
        xmlSerializer.startTag(ns, "properties");
        if ((properties != null) && (properties.size() > 0)) {
            for (CollectionProperty property : properties) {
                xmlSerializer.startTag(ns, "property");

                xmlSerializer.startTag(ns, "name");
                xmlSerializer.text(property.getName());
                xmlSerializer.endTag(ns, "name");

                xmlSerializer.startTag(ns, "description");
                if ((property.getDescription() != null) && (property.getDescription().length() > 0))
                    xmlSerializer.text(property.getDescription());
                xmlSerializer.endTag(ns, "description");

                xmlSerializer.endTag(ns, "property");
            }
        }
        xmlSerializer.endTag(ns, "properties");
    }

    private static void insertItems(XmlSerializer xmlSerializer, List<CollectionItem> items) throws IOException {
        xmlSerializer.startTag(ns, "items");
        if ((items != null) && (items.size() > 0)) {
            for (CollectionItem item : items) {
                xmlSerializer.startTag(ns, "item");
                // Add name
                xmlSerializer.startTag(ns, "name");
                xmlSerializer.text(item.getName());
                xmlSerializer.endTag(ns, "name");
                // Add description
                xmlSerializer.startTag(ns, "description");
                if ((item.getDescription() != null) && (item.getDescription().length() > 0))
                    xmlSerializer.text(item.getDescription());
                xmlSerializer.endTag(ns, "description");
                // Add properties
                for (int idx = 0; idx < item.getNbProperties(); idx++) {
                    xmlSerializer.startTag(ns, "property");
                    xmlSerializer.text(item.getProperty(idx));
                    xmlSerializer.endTag(ns, "property");
                }
                // Add images
                for (int idx = 0; idx < item.getNbImages(); idx++) {
                    xmlSerializer.startTag(ns, "img");
                    xmlSerializer.text(item.getImagePath(idx));
                    xmlSerializer.endTag(ns, "img");
                }
                xmlSerializer.endTag(ns, "item");
            }
        }
        xmlSerializer.endTag(ns, "items");
    }
}
