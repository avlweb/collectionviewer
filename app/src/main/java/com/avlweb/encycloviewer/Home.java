package com.avlweb.encycloviewer;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

public class Home extends Activity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayHomeAsUpEnabled(false);
            actionbar.setDisplayShowHomeEnabled(true);
        }

        // Check permissions because external access is mandatory
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        // Check that default database is present otherwise copy it to default storage
        File defaultPath = this.getExternalFilesDir(null);
        File defaultDir = new File(defaultPath.getAbsolutePath() + File.separator + "Default");
        Log.d("HOME", "Default dir = " + defaultDir.getAbsolutePath());
        if (!defaultDir.exists()) {
            Log.d("HOME", "Default dir does not exists");
            File defaultImages = new File(defaultDir, "images");
            boolean mkdirResult = defaultImages.mkdirs();
            Log.d("HOME", "mkdir result = " + mkdirResult);
            AssetManager assetManager = getAssets();
            String[] assets;
            try {
                assets = assetManager.list("Default");
                for (String asset : assets) {
                    Log.d("HOME", "asset = " + asset);
                    if (asset.endsWith(".xml")) {
                        copyFileFromAssets(assetManager, asset, defaultDir.getAbsolutePath());
                    } else {
                        copyFileFromAssets(assetManager, asset, defaultImages.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("HOME", "Default dir exists");
        }

        // Build list of available databases
        ArrayList<String> allFiles = new ArrayList<>();
        // Get preferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
        // Get flag "Hide sample database"
        boolean hideSampledatabase = pref.getBoolean(Settings.KEY_HIDE_SAMPLE_DATABASE, false);
        // Get Databases Root location
        String databasesRootLocation = pref.getString(Settings.KEY_DATABASES_ROOT_LOCATION, this.getExternalFilesDir(null).toString());
        // Get Scrollbar position
        int scrollbarPosition = pref.getInt(Settings.KEY_SCROLLBAR, 0);

        // Add databases found in databases root location
        getFilesRec(allFiles, databasesRootLocation, true);
        // Add default database if not deactivated or if there are no other database
        if ((allFiles.size() == 0) || !hideSampledatabase)
            getFilesRec(allFiles, defaultDir.getAbsolutePath(), false);

        // Populate list of databases
        ListView lv = findViewById(R.id.listView);
        String[] lv_arr = allFiles.toArray(new String[0]);
        lv.setAdapter(new ArrayAdapter<>(this, R.layout.mainlist, lv_arr));
        if (scrollbarPosition == 1)
            lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
        else
            lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = (String) ((TextView) view).getText();
                if (MainList.itemsList != null)
                    MainList.itemsList.clear();
                MainList.itemsList = null;
                MainList.itemsList = readListFromXml(path);
                Toast.makeText(getApplicationContext(), "Database '" + path + "' has been loaded.", Toast.LENGTH_SHORT).show();
                path = new File(path).getParent();
                MainList.dbpath = path;

                openDatabase(null);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.menu_settings:
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;

            case R.id.menu_version:
                Context context = getApplicationContext();
                PackageManager packageManager = context.getPackageManager();
                String packageName = context.getPackageName();
                String versionName = getString(R.string.not_available);
                try {
                    versionName = packageManager.getPackageInfo(packageName, 0).versionName;
                } catch (PackageManager.NameNotFoundException ignored) {
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
                String buildDate = simpleDateFormat.format(BuildConfig.buildTime);

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.version));
                builder.setMessage(getString(R.string.name_points) + context.getString(R.string.app_name)
                        + "\n" +
                        getString(R.string.package_points) + packageName
                        + "\n" +
                        getString(R.string.version_points) + versionName
                        + "\n" +
                        getString(R.string.build_date_points) + buildDate);
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {// If request is cancelled, the result arrays are empty.
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permission was granted, yay!
            } else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.warning_points);
                alertDialogBuilder.setMessage(R.string.warning_permission);
                alertDialogBuilder.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                finishAndRemoveTask();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }

    private void copyFileFromAssets(AssetManager assetManager, String file2Copy, String destDir) {
        FileChannel in_chan = null, out_chan = null;
        try {
            AssetFileDescriptor in_afd = assetManager.openFd("Default/" + file2Copy);
            FileInputStream in_stream = in_afd.createInputStream();
            in_chan = in_stream.getChannel();
            Log.d("HOME", "Asset space in file : start = " + in_afd.getStartOffset() + ", length = " + in_afd.getLength());
            File out_file = new File(destDir + File.separator + file2Copy);
            Log.d("HOME", "out file : " + out_file.getAbsolutePath());
            FileOutputStream out_stream = new FileOutputStream(out_file);
            out_chan = out_stream.getChannel();
            in_chan.transferTo(in_afd.getStartOffset(), in_afd.getLength(), out_chan);
        } catch (IOException ioe) {
            Log.w("HOME", "Failed to copy file '" + file2Copy + "' to external storage : " + ioe.toString());
        } finally {
            try {
                if (in_chan != null) {
                    in_chan.close();
                }
                if (out_chan != null) {
                    out_chan.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void openDatabase(View view) {
        Intent intent = new Intent(this, MainList.class);
        startActivity(intent);
    }

    private void getFilesRec(ArrayList<String> files, String root, boolean skipDefault) {
        File f = new File(root);
        File[] listFiles = f.listFiles();

        if ((listFiles != null) && (listFiles.length > 0)) {
            for (File listFile : listFiles) {
                String fname = listFile.toString();
                if (listFile.isDirectory())
                    getFilesRec(files, fname, skipDefault);
                else if (fname.endsWith(".xml")) {
                    if (fname.endsWith("Sample_database.xml") && skipDefault)
                        continue;
                    files.add(fname);
                }
            }
        }
    }

    public ArrayList<DbItem> readListFromXml(String path) {
        ArrayList<DbItem> itemsList = new ArrayList<>();
        FileInputStream fin = null;
        InputStreamReader isr = null;

        XmlPullParser xmlFile = Xml.newPullParser();
        try {
            //fIn = openFileInput( path);
            fin = new FileInputStream(path);
            isr = new InputStreamReader(fin);
            xmlFile.setInput(isr);

            int eventType = xmlFile.getEventType();

            DbItem element = null;
            boolean enterElement = false;
            boolean enterContent = false;
            boolean enterField = false;
            // 1 = field1, 2 = field2, ... , 6 = images
            int type = 0;
            int fIndex = 0;
            String fName = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String strNode = xmlFile.getName();
                    if (strNode.equals("element")) {
                        enterElement = true;
                        element = new DbItem();
                    } else if (enterElement && (strNode.equals("field1")))
                        type = 1;
                    else if (enterElement && (strNode.equals("field2")))
                        type = 2;
                    else if (enterElement && (strNode.equals("field3")))
                        type = 3;
                    else if (enterElement && (strNode.equals("field4")))
                        type = 4;
                    else if (enterElement && (strNode.equals("field5")))
                        type = 5;
                    else if (enterElement && (strNode.equals("img")))
                        type = 6;
                    else if (strNode.equals("content")) {
                        enterContent = true;
                        MainList.dbInfos = new DatabaseInfos();
                    } else if (enterContent && (strNode.equals("name")))
                        type = 1;
                    else if (enterContent && (strNode.equals("description")))
                        type = 2;
                    else if (enterContent && (strNode.equals("version")))
                        type = 3;
                    else if (strNode.equals("field")) {
                        enterField = true;
                    } else if (enterField && (strNode.equals("name")))
                        type = 1;
                    else if (enterField && (strNode.equals("number")))
                        type = 2;
                    else if (enterField && (strNode.equals("description")))
                        type = 3;
                } else if (eventType == XmlPullParser.END_TAG) {
                    String strNode = xmlFile.getName();
                    switch (strNode) {
                        case "element":
                            itemsList.add(element);
                            enterElement = false;
                            break;
                        case "content":
                            enterContent = false;
                            break;
                        case "field":
                            if (fIndex < 1 || fIndex > 5)
                                Toast.makeText(getApplicationContext(),
                                        "Field error : index = " + fIndex + ", name = " + fName,
                                        Toast.LENGTH_LONG).show();

                            MainList.dbInfos.setFieldName(fName, fIndex);
                            enterField = false;
                            break;
                    }
                    type = 0;
                } else if (eventType == XmlPullParser.TEXT) {
                    if (enterElement) {
/*
                        int nbAttribute = xmlFile.getAttributeCount();
                        Log.d("HOME", "Number of attributes = " + nbAttribute);
                        for (int i = 0; i < nbAttribute; i++) {
                            Log.d("HOME", "Attribute : name = " + xmlFile.getAttributeName(i) + ", value = " + xmlFile.getAttributeValue(i));
                        }
*/
                        switch (type) {
                            case 1:
                                element.setField1(xmlFile.getText());
                                break;
                            case 2:
                                element.setField2(xmlFile.getText());
                                break;
                            case 3:
                                element.setField3(xmlFile.getText());
                                break;
                            case 4:
                                element.setField4(xmlFile.getText());
                                break;
                            case 5:
                                element.setField5(xmlFile.getText());
                                break;
                            case 6:
                                element.addImagePath(xmlFile.getText());
                                break;
                        }
                    } else if (enterContent) {
                        switch (type) {
                            case 1:
                                MainList.dbInfos.setName(xmlFile.getText());
                                break;
                            case 2:
                                MainList.dbInfos.setDescription(xmlFile.getText());
                                break;
                            case 3:
                                MainList.dbInfos.setVersion(xmlFile.getText());
                                break;
                        }
                    } else if (enterField) {
                        switch (type) {
                            case 1:
                                fName = xmlFile.getText();
                                break;
                            case 2:
                                fIndex = Integer.parseInt(xmlFile.getText());
                                break;
                            case 3:
                                break;
                        }
                    }
                }

                eventType = xmlFile.next();
            }
        } catch (XmlPullParserException | IOException e1) {
            e1.printStackTrace();
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

        return itemsList;
    }
}
