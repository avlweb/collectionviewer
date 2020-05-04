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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.avlweb.encycloviewer.BuildConfig;
import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.adapter.HomeListAdapter;
import com.avlweb.encycloviewer.model.DatabaseInfos;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.util.xmlFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.avlweb.encycloviewer.ui.Settings.KEY_DATABASES_ROOT_LOCATION;
import static com.avlweb.encycloviewer.ui.Settings.KEY_HIDE_SAMPLE_DATABASE;
import static com.avlweb.encycloviewer.ui.Settings.KEY_PREFS;

public class Home extends BaseActivity implements HomeListAdapter.customButtonListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 1;
    private ArrayList<String> xmlfiles = new ArrayList<>();
    private String selectedDatabase;
    private HomeListAdapter adapter;

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
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE);
        }

        displayHelpButton();

        // Build list of available databases
        // Check that default database is present otherwise we copy it to default storage
        File defaultPath = this.getExternalFilesDir(null);
        String databasesRootLocation = null;
        if (defaultPath != null) {
            File defaultDir = new File(defaultPath.getAbsolutePath() + File.separator + "Default");
            checkDefaultDatabase(defaultDir);

            // Get preferences
            SharedPreferences pref = getApplicationContext().getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
            // Get flag "Hide sample database"
            boolean hideSampledatabase = pref.getBoolean(KEY_HIDE_SAMPLE_DATABASE, false);
            // Get Databases Root location
            databasesRootLocation = pref.getString(KEY_DATABASES_ROOT_LOCATION, defaultPath.getPath());

            // Add databases found in databases root location
            getFilesRec(xmlfiles, databasesRootLocation, true);
            // Add default database if not deactivated or if there are no other database
            if ((xmlfiles.size() == 0) || !hideSampledatabase)
                getFilesRec(xmlfiles, defaultDir.getAbsolutePath(), false);
        }

        // Populate list of databases
        ListView lv = findViewById(R.id.listView);
        adapter = new HomeListAdapter(this, xmlfiles, databasesRootLocation);
        adapter.setCustomButtonListener(this);
        lv.setAdapter(adapter);
        registerForContextMenu(lv);
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

            case R.id.menu_add:
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialog = inflater.inflate(R.layout.dialog_new_something, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(getString(R.string.new_database));
                alertDialog.setMessage(getString(R.string.message_new_database));
                alertDialog.setCancelable(false);
                final EditText propertyName = dialog.findViewById(R.id.propertyName);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideKeyboard();
                        String name = propertyName.getText().toString();
                        if (name.length() > 0) {
                            createNewDatabase(name);
                        }
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setView(dialog);
                alertDialog.show();
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
                builder.setMessage(String.format(Locale.getDefault(), getString(R.string.appli_version),
                        context.getString(R.string.app_name), packageName, versionName, buildDate));
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
    public void onButtonClickListener(View view, int position, String value) {
        this.selectedDatabase = value;
        openContextMenu(view);
    }

    @Override
    public void onTextClickListener(int position, String path) {
        EncycloDatabase database = EncycloDatabase.getInstance();
        database.clear();
        xmlFactory.readXMLFile(path);
        Toast.makeText(getApplicationContext(), "Database '" + path + "' has been loaded.", Toast.LENGTH_SHORT).show();
        database.getInfos().setPath(new File(path).getParent());
        database.getInfos().setXmlPath(path);

        Intent intent = new Intent(this, MainList.class);
        startActivity(intent);
    }

    private void createNewDatabase(String name) {
        // Initialize new database
        EncycloDatabase database = EncycloDatabase.getInstance();
        database.clear();

        // Set database infos
        DatabaseInfos infos = new DatabaseInfos();
        infos.setName(name);
        infos.setVersion("1.0");
        database.setInfos(infos);

        // Generate database XML file in default root location
        // Get preferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        // Get Default External location
        File defaultPath = this.getExternalFilesDir(null);
        if (defaultPath != null) {
            // Get Databases Root location from preferences
            String databasesRootLocation = pref.getString(KEY_DATABASES_ROOT_LOCATION, defaultPath.getAbsolutePath());
            // Format DB name and directory name
            String dbName = name.toLowerCase().replace(" ", "_");
            String dbDirectory = name.toUpperCase().replace(" ", "_");
            String dbPath = databasesRootLocation + File.separatorChar + dbDirectory;
            // Create new database directory
            File defaultImagesPath = new File(dbPath, "images");
            if (defaultImagesPath.mkdirs()) {
                String xmlPath = dbPath + File.separatorChar + dbName + ".xml";
                database.getInfos().setXmlPath(xmlPath);
                database.getInfos().setPath(new File(xmlPath).getParent());
                // Add new XML file to list to refresh adapter
                xmlfiles.add(xmlPath);
                // Write XML file
                if (xmlFactory.writeXml()) {
                    Toast.makeText(getApplicationContext(), R.string.database_successfully_created, Toast.LENGTH_SHORT).show();
                    // Display activity to modify the database
                    Intent intent = new Intent(this, DatabaseModify.class);
                    startActivity(intent);
                } else
                    Toast.makeText(getApplicationContext(), R.string.database_creation_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (!((grantResults.length >= 2) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_home_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_modify:
                // Load database
                EncycloDatabase database = EncycloDatabase.getInstance();
                database.clear();
                xmlFactory.readXMLFile(this.selectedDatabase);
                database.getInfos().setXmlPath(this.selectedDatabase);
                database.getInfos().setPath(new File(this.selectedDatabase).getParent());
                // Display activity to modify the database
                Intent intent = new Intent(this, DatabaseModify.class);
                startActivity(intent);
                return true;
            case R.id.menu_delete:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.warning);
                alertDialogBuilder.setIcon(R.drawable.ic_warning);
                alertDialogBuilder.setMessage(R.string.warning_database_deletion);
                alertDialogBuilder.setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                File databaseDir = new File(selectedDatabase);
                                if (deleteRecursive(databaseDir.getParentFile())) {
                                    removeDatabaseFromList(selectedDatabase);
                                    Toast.makeText(getApplicationContext(), R.string.database_deletion_successful, Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(getApplicationContext(), R.string.deletion_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void removeDatabaseFromList(String database) {
        if ((xmlfiles != null) && (database != null)) {
            xmlfiles.remove(database);
            adapter.remove(database);
        }
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory()) {
                File[] files = fileOrDirectory.listFiles();
                if (files != null) {
                    for (File child : files)
                        deleteRecursive(child);
                }
            }
            return fileOrDirectory.delete();
        }
        return false;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
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
                    if (asset.endsWith("Sample_database.xml")) {
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
}
