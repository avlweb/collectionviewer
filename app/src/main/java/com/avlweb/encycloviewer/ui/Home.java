package com.avlweb.encycloviewer.ui;

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

import com.avlweb.encycloviewer.BuildConfig;
import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DatabaseInfos;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.FieldDescription;

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

        // Build list of available databases
        ArrayList<String> allFiles = new ArrayList<>();

        // Check that default database is present otherwise we copy it to default storage
        File defaultPath = this.getExternalFilesDir(null);
        if (defaultPath != null) {
            File defaultDir = new File(defaultPath.getAbsolutePath() + File.separator + "Default");
            checkDefaultDatabase(defaultDir);

            // Get preferences
            SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
            // Get flag "Hide sample database"
            boolean hideSampledatabase = pref.getBoolean(Settings.KEY_HIDE_SAMPLE_DATABASE, false);
            // Get Databases Root location
            String databasesRootLocation = pref.getString(Settings.KEY_DATABASES_ROOT_LOCATION, defaultPath.getPath());

            // Add databases found in databases root location
            getFilesRec(allFiles, databasesRootLocation, true);
            // Add default database if not deactivated or if there are no other database
            if ((allFiles.size() == 0) || !hideSampledatabase)
                getFilesRec(allFiles, defaultDir.getAbsolutePath(), false);
        }

        // Populate list of databases
        ListView lv = findViewById(R.id.listView);
        String[] lv_arr = allFiles.toArray(new String[0]);
        lv.setAdapter(new ArrayAdapter<>(this, R.layout.my_main_list, lv_arr));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = (String) ((TextView) view).getText();
                if (MainList.itemsList != null)
                    MainList.itemsList.clear();
                MainList.itemsList = null;
                MainList.itemsList = readXMLFile(path);
                Toast.makeText(getApplicationContext(), "Database '" + path + "' has been loaded.", Toast.LENGTH_SHORT).show();
                path = new File(path).getParent();
                MainList.dbpath = path;
                MainList.selectedItemPosition = 0;

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
                builder.setIcon(R.drawable.ic_launcher);
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
                alertDialogBuilder.setIcon(R.drawable.ic_launcher);
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

    private void checkDefaultDatabase(File defaultPath) {
        Log.d("HOME", "Default dir = " + defaultPath.getAbsolutePath());
        if (!defaultPath.exists()) {
            Log.d("HOME", "Default dir does not exists");
            File defaultImages = new File(defaultPath, "images");
            boolean mkdirResult = defaultImages.mkdirs();
            Log.d("HOME", "mkdir result = " + mkdirResult);
            AssetManager assetManager = getAssets();
            try {
                String[] assets = assetManager.list("Default");
                if (assets == null)
                    return;
                for (String asset : assets) {
                    Log.d("HOME", "asset = " + asset);
                    if (asset.endsWith(".xml")) {
                        copyFileFromAssets(assetManager, asset, defaultPath.getAbsolutePath());
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

    public ArrayList<DbItem> readXMLFile(String path) {
        ArrayList<DbItem> itemsList = new ArrayList<>();
        FileInputStream fin = null;
        InputStreamReader isr = null;

        XmlPullParser xmlFile = Xml.newPullParser();
        try {
            fin = new FileInputStream(path);
            isr = new InputStreamReader(fin);
            xmlFile.setInput(isr);

            int eventType = xmlFile.getEventType();

            DbItem element = null;
            FieldDescription field = null;
            boolean enterElement = false;
            boolean enterContent = false;
            boolean enterFielddesc = false;
            // 1 = field, 2 = images
            int type = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String strNode = xmlFile.getName();
                    if (strNode.equals("content")) {
                        enterContent = true;
                        enterFielddesc = false;
                        enterElement = false;
                        MainList.dbInfos = new DatabaseInfos();
                    } else if (strNode.equals("fielddesc")) {
                        enterContent = false;
                        enterFielddesc = true;
                        enterElement = false;
                        field = new FieldDescription();
                    } else if (strNode.equals("element")) {
                        enterContent = false;
                        enterFielddesc = false;
                        enterElement = true;
                        element = new DbItem();
                    } else if (enterElement) {
                        switch (strNode) {
                            case "field":
                                type = 1;
                                break;
                            case "img":
                                type = 2;
                                break;
                        }
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
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (enterElement) {
                        switch (type) {
                            case 1:
                                element.addField(xmlFile.getText());
                                break;
                            case 2:
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
                    } else if (enterFielddesc) {
                        switch (type) {
                            case 1:
                                field.setName(xmlFile.getText());
                                field.setId(View.generateViewId());
                                break;
                            case 2:
                                field.setDescription(xmlFile.getText());
                                break;
                        }
                    }
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
                        case "fielddesc":
                            MainList.dbInfos.addFieldDescription(field);
                            enterFielddesc = false;
                            break;
                    }
                    type = 0;
                }

                eventType = xmlFile.next();
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

        return itemsList;
    }
}