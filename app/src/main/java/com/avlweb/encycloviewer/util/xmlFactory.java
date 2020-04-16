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

            boolean enterItem = false;
            boolean enterContent = false;
            boolean enterFielddesc = false;
            // 1 = field, 2 = images
            int type = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String strNode = parser.getName();
                    if (strNode.equals("content")) {
                        enterContent = true;
                        enterFielddesc = false;
                        enterItem = false;
                        dbInfos = new DatabaseInfos();
                    } else if (strNode.equals("fielddesc")) {
                        enterContent = false;
                        enterFielddesc = true;
                        enterItem = false;
                        field = new FieldDescription();
                    } else if (strNode.equals("item")) {
                        enterContent = false;
                        enterFielddesc = false;
                        enterItem = true;
                        item = new DbItem();
                    } else if (enterContent) {
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
                    } else if (enterFielddesc) {
                        switch (strNode) {
                            case "name":
                                type = 1;
                                break;
                            case "description":
                                type = 2;
                                break;
                        }
                    } else if (enterItem) {
                        switch (strNode) {
                            case "field":
                                type = 1;
                                break;
                            case "img":
                                type = 2;
                                break;
                        }
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (enterContent) {
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
                    } else if (enterFielddesc) {
                        switch (type) {
                            case 1:
                                field.setName(parser.getText());
                                field.setId(View.generateViewId());
                                break;
                            case 2:
                                field.setDescription(parser.getText());
                                break;
                        }
                    } else if (enterItem) {
                        switch (type) {
                            case 1:
                                item.addField(parser.getText());
                                break;
                            case 2:
                                item.addImagePath(parser.getText());
                                break;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String strNode = parser.getName();
                    switch (strNode) {
                        case "content":
                            database.setInfos(dbInfos);
                            enterContent = false;
                            break;
                        case "fielddesc":
                            database.addFieldDescription(field);
                            enterFielddesc = false;
                            break;
                        case "item":
                            database.addItemToList(item);
                            enterItem = false;
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

    public static void writeXml() {

        EncycloDatabase database = EncycloDatabase.getInstance();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(database.getInfos().getPath());
            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            StringWriter writer = new StringWriter();

            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(ns, "database");

            insertContent(xmlSerializer, database.getInfos());
            insertFields(xmlSerializer, database.getFieldDescriptions());
            insertItems(xmlSerializer, database.getItemsList());

            xmlSerializer.endTag(ns, "database");
            xmlSerializer.endDocument();
            xmlSerializer.flush();

            String dataWrite = writer.toString();
            fileOutputStream.write(dataWrite.getBytes());
            fileOutputStream.close();

        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void insertContent(XmlSerializer xmlSerializer, DatabaseInfos dbInfos) throws IOException {
        xmlSerializer.startTag(ns, "content");

        xmlSerializer.startTag(ns, "name");
        xmlSerializer.text(dbInfos.getName());
        xmlSerializer.endTag(ns, "name");

        xmlSerializer.startTag(ns, "description");
        xmlSerializer.text(dbInfos.getDescription());
        xmlSerializer.endTag(ns, "description");

        xmlSerializer.startTag(ns, "version");
        xmlSerializer.text(dbInfos.getVersion());
        xmlSerializer.endTag(ns, "version");

        xmlSerializer.endTag(ns, "content");
    }

    private static void insertFields(XmlSerializer xmlSerializer, List<FieldDescription> dbInfos) throws IOException {
        xmlSerializer.startTag(ns, "fielddescs");
        for (FieldDescription desc : dbInfos) {
            xmlSerializer.startTag(ns, "fielddesc");

            xmlSerializer.startTag(ns, "name");
            xmlSerializer.text(desc.getName());
            xmlSerializer.endTag(ns, "name");

            xmlSerializer.startTag(ns, "description");
            xmlSerializer.text(desc.getDescription());
            xmlSerializer.endTag(ns, "description");

            xmlSerializer.endTag(ns, "fielddesc");
        }
        xmlSerializer.endTag(ns, "fielddescs");
    }

    private static void insertItems(XmlSerializer xmlSerializer, List<DbItem> items) throws IOException {
        xmlSerializer.startTag(ns, "items");
        for (DbItem item : items) {
            xmlSerializer.startTag(ns, "item");
            for (int idx = 0; idx < item.getNbFields(); idx++) {
                xmlSerializer.startTag(ns, "field");
                xmlSerializer.text(item.getField(idx));
                xmlSerializer.endTag(ns, "field");
            }
            for (int idx = 0; idx < item.getNbImages(); idx++) {
                xmlSerializer.startTag(ns, "img");
                xmlSerializer.text(item.getImagePath(idx));
                xmlSerializer.endTag(ns, "img");
            }
            xmlSerializer.endTag(ns, "item");
        }
        xmlSerializer.endTag(ns, "items");
    }
}
