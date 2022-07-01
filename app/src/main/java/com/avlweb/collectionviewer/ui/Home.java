package com.avlweb.collectionviewer.ui;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.avlweb.collectionviewer.BuildConfig;
import com.avlweb.collectionviewer.R;
import com.avlweb.collectionviewer.adapter.HomeListAdapter;
import com.avlweb.collectionviewer.model.CollectionInfos;
import com.avlweb.collectionviewer.model.CollectionModel;
import com.avlweb.collectionviewer.util.XmlFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Home extends BaseActivity implements HomeListAdapter.customButtonListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 1;
    private final ArrayList<CollectionInfos> availableCollections = new ArrayList<>();
    private CollectionInfos selectedCollection;
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

        // Build list of available collections and check that default collection is present otherwise we copy it to default storage
        File defaultPath = this.getExternalFilesDir(null);
        if (defaultPath != null) {
            // Build default collection path
            File defaultDir = new File(defaultPath.getAbsolutePath() + File.separator + "Default");
            // Copy default collection if needed
            checkDefaultCollection(defaultDir);

            // Get preferences
            SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
            // Get flag "Hide sample collection"
            boolean hideSampleCollection = pref.getBoolean(Settings.KEY_HIDE_SAMPLE_COLLECTION, false);
            // Get Collections Root location
            String collectionsRootLocation = pref.getString(Settings.KEY_COLLECTIONS_ROOT_LOCATION, defaultPath.getPath());

            // Add collections found in root location
            getFilesRecursive(availableCollections, collectionsRootLocation, true);

            // Add default collection if not deactivated or if there are no other collection already created
            if ((availableCollections.size() == 0) || !hideSampleCollection)
                getFilesRecursive(availableCollections, defaultDir.getAbsolutePath(), false);
        }

        // Set scrollbar position according to settings
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
        int scrollbarPosition = pref.getInt(Settings.KEY_SCROLLBAR, 0);
        ListView lv = findViewById(R.id.listViewHome);
        if (scrollbarPosition == 1)
            lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
        else
            lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);

        // Populate list of collections
        adapter = new HomeListAdapter(this, availableCollections, scrollbarPosition);
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
            case (android.R.id.home):
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case (R.id.menu_settings):
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;

            case (R.id.menu_add):
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_new_something);
                dialog.setTitle(getString(R.string.new_collection));

                TextView textView = dialog.findViewById(R.id.message);
                textView.setText(R.string.message_new_collection);

                Button btnOK = dialog.findViewById(R.id.btn_ok);
                Button btnCancel = dialog.findViewById(R.id.btn_cancel);
                btnOK.setOnClickListener(view -> {
                    EditText propertyName = dialog.findViewById(R.id.propertyName);
                    String name = propertyName.getText().toString();
                    if (name.isEmpty()) {
                        propertyName.setError(getString(R.string.must_not_be_empty));
                        return;
                    }
                    if (!name.matches("([A-Za-z0-9]|-)+")) {
                        propertyName.setError(getString(R.string.must_contains_alphabet));
                        return;
                    }
                    dialog.dismiss();
                    addCollection(name);
                });
                btnCancel.setOnClickListener(view -> {
                    hideKeyboard();
                    dialog.cancel();
                });
                dialog.setCancelable(false);
                dialog.show();
                return true;

            case (R.id.menu_version):
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
                        context.getString(R.string.app_name), versionName, buildDate));
                builder.setPositiveButton(getString(R.string.ok), (dialog1, which) -> dialog1.cancel());
                builder.create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onButtonClickListener(View view, CollectionInfos infos) {
        this.selectedCollection = infos;
        openContextMenu(view);
    }

    @Override
    public void onTextClickListener(CollectionInfos infos) {
        // Load selected collection
        XmlFactory.readXMLFile(infos.getXmlPath());
        Toast.makeText(getApplicationContext(),
                String.format(Locale.getDefault(), getString(R.string.collection_loaded), infos.getName()), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainList.class);
        startActivity(intent);
    }

    private void addCollection(String name) {
        // Initialize new collection
        CollectionModel collectionModel = CollectionModel.getInstance();
        collectionModel.clear();

        // Set collection infos
        CollectionInfos infos = new CollectionInfos();
        infos.setName(name);
        infos.setVersion("1.0");
        collectionModel.setInfos(infos);

        // Generate collection XML file in default root location
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
        // Get Default External location
        File defaultPath = this.getExternalFilesDir(null);
        if (defaultPath != null) {
            // Get Collections Root location from preferences
            String collectionsRootLocation = pref.getString(Settings.KEY_COLLECTIONS_ROOT_LOCATION, defaultPath.getAbsolutePath());
            // Format DB name and directory name
            String dbName = name.toLowerCase().replace(" ", "_");
            String dbDirectory = name.toUpperCase().replace(" ", "_");
            String dbPath = collectionsRootLocation + File.separatorChar + dbDirectory;
            // Create new collection directory
            File defaultImagesPath = new File(dbPath, "images");
            if (defaultImagesPath.mkdirs()) {
                String xmlPath = dbPath + File.separatorChar + dbName + ".xml";
                // Add new XML file to list to refresh adapter
                infos.setXmlPath(xmlPath);
                infos.setPath(new File(xmlPath).getParent());
                availableCollections.add(infos);
                // Write XML file
                if (XmlFactory.writeXml()) {
                    Toast.makeText(getApplicationContext(), R.string.collection_successfully_created, Toast.LENGTH_SHORT).show();
                    // Display activity to modify the collection
                    Intent intent = new Intent(this, CollectionModify.class);
                    startActivity(intent);
                } else
                    Toast.makeText(getApplicationContext(), R.string.collection_creation_error, Toast.LENGTH_SHORT).show();
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
                        (arg0, arg1) -> finishAndRemoveTask());
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
            case (R.id.menu_modify):
                // Load selected collection
                XmlFactory.readXMLFile(this.selectedCollection.getXmlPath());
                // Display activity to modify the collection
                Intent intent = new Intent(this, CollectionModify.class);
                startActivity(intent);
                return true;
            case (R.id.menu_delete):
                // Display dialog to ask user to confirm the deletion
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.warning);
                alertDialogBuilder.setIcon(R.drawable.ic_warning);
                alertDialogBuilder.setMessage(R.string.warning_collection_deletion);
                alertDialogBuilder.setNegativeButton(getString(R.string.no),
                        (dialog, arg1) -> dialog.cancel());
                alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                        (arg0, arg1) -> {
                            // Delete the collection
                            File collectionPath = new File(selectedCollection.getXmlPath());
                            if (deleteRecursive(collectionPath.getParentFile())) {
                                removeCollectionFromList(selectedCollection);
                                Toast.makeText(getApplicationContext(), R.string.collection_deletion_successful, Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getApplicationContext(), R.string.deletion_error, Toast.LENGTH_SHORT).show();
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void removeCollectionFromList(CollectionInfos infos) {
        if (infos != null) {
            availableCollections.remove(infos);
            adapter.remove(infos);
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

    private void checkDefaultCollection(File defaultPath) {
        Log.d("HOME", "Default dir = " + defaultPath.getAbsolutePath());
        if (!defaultPath.exists()) {
            Log.d("HOME", "Default dir does not exists");
            // Create directory for images
            File defaultImagePath = new File(defaultPath, "images");
            boolean mkdirResult = defaultImagePath.mkdirs();
            Log.d("HOME", "mkdir result = " + mkdirResult);
            AssetManager assetManager = getAssets();
            String language = Resources.getSystem().getConfiguration().locale.getLanguage();
            Log.d("HOME", "language = " + language);
            try {
                // Copy all files from asset "Default" directory
                String[] assets = assetManager.list("Default");
                if (assets == null)
                    return;
                for (String asset : assets) {
                    Log.d("HOME", "asset = " + asset);
                    // Destination path differs whatever it is an image or the xml file
                    if (asset.endsWith(".xml")) {
                        if (((language.equals("fr")) && (asset.endsWith(MainList.SAMPLE_COLLECTION_FR_XML)))
                                || ((!language.equals("fr")) && (asset.endsWith(MainList.SAMPLE_COLLECTION_EN_XML)))) {
                            copyFileFromAssets(assetManager, asset, defaultPath.getAbsolutePath());
                        }
                    } else {
                        copyFileFromAssets(assetManager, asset, defaultImagePath.getAbsolutePath());
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
        FileChannel inChannel = null, outChannel = null;
        try {
            AssetFileDescriptor in_afd = assetManager.openFd("Default/" + file2Copy);
            FileInputStream in_stream = in_afd.createInputStream();
            inChannel = in_stream.getChannel();
            Log.d("HOME", "Asset space in file : start = " + in_afd.getStartOffset() + ", length = " + in_afd.getLength());
            File out_file = new File(destDir + File.separator + file2Copy);
            Log.d("HOME", "out file : " + out_file.getAbsolutePath());
            FileOutputStream out_stream = new FileOutputStream(out_file);
            outChannel = out_stream.getChannel();
            inChannel.transferTo(in_afd.getStartOffset(), in_afd.getLength(), outChannel);
        } catch (IOException ioe) {
            Log.w("HOME", "Failed to copy file '" + file2Copy + "' to external storage : " + ioe);
        } finally {
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private void getFilesRecursive(ArrayList<CollectionInfos> files, String root, boolean skipDefault) {
        File f = new File(root);
        File[] listFiles = f.listFiles();

        if ((listFiles != null) && (listFiles.length > 0)) {
            for (File listFile : listFiles) {
                String fname = listFile.toString();
                if (listFile.isDirectory())
                    getFilesRecursive(files, fname, skipDefault);
                else if (fname.endsWith(".xml")) {
                    if (fname.endsWith(MainList.SAMPLE_COLLECTION_FR_XML) && skipDefault)
                        continue;
                    // Read XML file to get collection infos
                    CollectionInfos infos = XmlFactory.readCollectionInfos(fname);
                    infos.setXmlPath(fname);
                    infos.setPath(listFile.getParent());
                    files.add(infos);
                }
            }
        }
    }
}
