package com.avlweb.encycloviewer.util;

import android.util.Xml;
import android.view.View;

import com.avlweb.encycloviewer.model.DatabaseInfos;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.model.FieldDescription;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

public class xmlFactory {
    private static final String ns = null;

    public static void readXMLFile(String path) {
        EncycloDatabase database = EncycloDatabase.getInstance();
        FileInputStream fin = null;
        InputStreamReader isr = null;
        DbItem item = null;
        FieldDescription field = null;
        DatabaseInfos dbInfos = null;

        XmlPullParser parser = Xml.newPullParser();
        try {
            fin = new FileInputStream(path);
            isr = new InputStreamReader(fin);
            parser.setInput(isr);

            int eventType = parser.getEventType();

            boolean caseItem = false;
            boolean caseContent = false;
            boolean caseFielddesc = false;
            int type = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String strNode = parser.getName();
                    if (strNode.equals("content")) {
                        caseContent = true;
                        caseFielddesc = false;
                        caseItem = false;
                        dbInfos = new DatabaseInfos();
                    } else if (strNode.equals("fielddesc")) {
                        caseContent = false;
                        caseFielddesc = true;
                        caseItem = false;
                        field = new FieldDescription();
                    } else if (strNode.equals("item")) {
                        caseContent = false;
                        caseFielddesc = false;
                        caseItem = true;
                        item = new DbItem();
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
                    } else if (caseFielddesc) {
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
                            case "field":
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
                                dbInfos.setName(parser.getText());
                                break;
                            case 2:
                                dbInfos.setDescription(parser.getText());
                                break;
                            case 3:
                                dbInfos.setVersion(parser.getText());
                                break;
                        }
                    } else if (caseFielddesc) {
                        switch (type) {
                            case 1:
                                field.setName(parser.getText());
                                field.setId(View.generateViewId());
                                break;
                            case 2:
                                field.setDescription(parser.getText());
                                break;
                        }
                    } else if (caseItem) {
                        switch (type) {
                            case 1:
                                item.addField(parser.getText());
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
                    switch (strNode) {
                        case "content":
                            database.setInfos(dbInfos);
                            caseContent = false;
                            break;
                        case "fielddesc":
                            database.addFieldDescription(field);
                            caseFielddesc = false;
                            break;
                        case "item":
                            database.addItemToList(item);
                            caseItem = false;
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
    }

    public static boolean writeXml() {

        EncycloDatabase database = EncycloDatabase.getInstance();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(database.getInfos().getXmlPath());
            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            StringWriter writer = new StringWriter();

            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(ns, "database");

            insertDatabaseInfos(xmlSerializer, database.getInfos());
            insertFields(xmlSerializer, database.getFieldDescriptions());
            insertItems(xmlSerializer, database.getItemsList());

            xmlSerializer.endTag(ns, "database");
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

    private static void insertDatabaseInfos(XmlSerializer xmlSerializer, DatabaseInfos dbInfos) throws IOException {
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

    private static void insertFields(XmlSerializer xmlSerializer, List<FieldDescription> dbInfos) throws IOException {
        xmlSerializer.startTag(ns, "fielddescs");
        if ((dbInfos != null) && (dbInfos.size() > 0)) {
            for (FieldDescription desc : dbInfos) {
                xmlSerializer.startTag(ns, "fielddesc");

                xmlSerializer.startTag(ns, "name");
                xmlSerializer.text(desc.getName());
                xmlSerializer.endTag(ns, "name");

                xmlSerializer.startTag(ns, "description");
                if ((desc.getDescription() != null) && (desc.getDescription().length() > 0))
                    xmlSerializer.text(desc.getDescription());
                xmlSerializer.endTag(ns, "description");

                xmlSerializer.endTag(ns, "fielddesc");
            }
        }
        xmlSerializer.endTag(ns, "fielddescs");
    }

    private static void insertItems(XmlSerializer xmlSerializer, List<DbItem> items) throws IOException {
        xmlSerializer.startTag(ns, "items");
        if ((items != null) && (items.size() > 0)) {
            for (DbItem item : items) {
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
                // Add fields
                for (int idx = 0; idx < item.getNbFields(); idx++) {
                    xmlSerializer.startTag(ns, "field");
                    xmlSerializer.text(item.getField(idx));
                    xmlSerializer.endTag(ns, "field");
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
