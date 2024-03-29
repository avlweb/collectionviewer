package com.avlweb.collectionviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;
import androidx.documentfile.provider.DocumentFile;

import com.avlweb.collectionviewer.R;

import java.io.File;
import java.util.Arrays;

public class Settings extends Activity {

    public static final String KEY_PREFS = "CollectionViewerPreferences";
    public static final String KEY_COLLECTIONS_ROOT_LOCATION = "key_root_location";
    public static final String KEY_HIDE_SAMPLE_COLLECTION = "key_hide_sample";
    public static final String KEY_HIDE_HELP_BUTTON = "key_hide_help";
    public static final String KEY_SCROLLBAR = "key_scrollbar";
    public static final String KEY_FONT_SIZE = "key_font_size";
    private final int OPEN_DIRECTORY_REQUEST_CODE = 142587484;

    private int fontSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(R.string.settings);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        // Get preferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        // Get flag "Hide sample collection"
        boolean hideSampleCollection = pref.getBoolean(KEY_HIDE_SAMPLE_COLLECTION, false);
        Switch hide = findViewById(R.id.switch_hide);
        hide.setChecked(hideSampleCollection);
        // Get flag "Hide help button"
        boolean hideHelpButton = pref.getBoolean(KEY_HIDE_HELP_BUTTON, false);
        Switch help = findViewById(R.id.switch_help);
        help.setChecked(hideHelpButton);
        // Get Collections Root location
        File defaultPath = this.getExternalFilesDir(null);
        if (defaultPath != null) {
            String collectionsRootLocation = pref.getString(KEY_COLLECTIONS_ROOT_LOCATION, defaultPath.toString());
            TextView textView = findViewById(R.id.EditTextRootLocation);
            textView.setText(collectionsRootLocation);
        }
        // Get Scrollbar position
        int scrollbar = pref.getInt(KEY_SCROLLBAR, 0);
        RadioButton buttonScrollbar;
        if (scrollbar == 0) {
            buttonScrollbar = findViewById(R.id.radioButtonLeft);
        } else {
            buttonScrollbar = findViewById(R.id.radioButtonRight);
        }
        buttonScrollbar.setChecked(true);
        // Font size
        fontSize = pref.getInt(KEY_FONT_SIZE, 0);
        TextView textSeekbar = findViewById(R.id.TextLorum);
        textSeekbar.setTextSize(getFontSizeFromPref(fontSize));
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> doOnFontSizeChanged(group));
        RadioButton button = findViewById(R.id.radioButtonSmall);
        switch (fontSize) {
            case 1:
                button = findViewById(R.id.radioButtonNormal);
                break;
            case 2:
                button = findViewById(R.id.radioButtonBig);
                break;
            case 3:
                button = findViewById(R.id.radioButtonVerybig);
                break;
        }
        button.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_item_modify, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case (R.id.save_btn):
                saveSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doOnFontSizeChanged(RadioGroup group) {
        int checkedRadioId = group.getCheckedRadioButtonId();
        switch (checkedRadioId) {
            case (R.id.radioButtonSmall):
                fontSize = 0;
                break;
            case (R.id.radioButtonNormal):
                fontSize = 1;
                break;
            case (R.id.radioButtonBig):
                fontSize = 2;
                break;
            case (R.id.radioButtonVerybig):
                fontSize = 3;
                break;
        }
        TextView textSeekbar = findViewById(R.id.TextLorum);
        textSeekbar.setTextSize(getFontSizeFromPref(fontSize));
    }

    public static int getFontSizeFromPref(int val) {
        int res;
        switch (val) {
            case 1:
                res = 16;
                break;
            case 2:
                res = 18;
                break;
            case 3:
                res = 20;
                break;
            default:
                res = 14;
                break;
        }
        return res;
    }

    public void saveSettings() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        // Collections Root location
        EditText rootLocation = findViewById(R.id.EditTextRootLocation);
        editor.putString(KEY_COLLECTIONS_ROOT_LOCATION, rootLocation.getText().toString());
        // Scrollbar position
        RadioButton buttonScrollbar = findViewById(R.id.radioButtonLeft);
        editor.putInt(KEY_SCROLLBAR, buttonScrollbar.isChecked() ? 0 : 1);
        // Font size
        editor.putInt(KEY_FONT_SIZE, fontSize);
        // Hide sample collection
        Switch hide = findViewById(R.id.switch_hide);
        editor.putBoolean(KEY_HIDE_SAMPLE_COLLECTION, hide.isChecked());
        // Hide help button
        Switch help = findViewById(R.id.switch_help);
        editor.putBoolean(KEY_HIDE_HELP_BUTTON, help.isChecked());
        // Save preferences
        editor.apply();
        Toast.makeText(getApplicationContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }

    public void searchFolder(View view) {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Provide read access to files and sub-directories in the user-selected directory.
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE);
    }

    public void resetFolder(View view) {
        // Reset collection directory path to default
        TextView textView = findViewById(R.id.EditTextRootLocation);
        textView.setText(this.getExternalFilesDir(null).getPath());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == OPEN_DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the directory that the user selected.
            if (resultData != null) {
                Uri uri = resultData.getData();
                if (uri != null) {
//                    getXmlFiles(uri);
                    File defaultPath = Environment.getExternalStorageDirectory();
                    Log.d("SETTINGS", "getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS) = " + Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS));
                    Log.d("SETTINGS", "getExternalStorageDirectory() = " + Environment.getExternalStorageDirectory());
                    Log.d("SETTINGS", "getExternalFilesDirs(DIRECTORY_DOCUMENTS) = " + Arrays.toString(this.getExternalFilesDirs(android.os.Environment.DIRECTORY_DOCUMENTS)));
                    if (defaultPath != null) {
                        Log.d("SETTINGS", "default path = " + defaultPath.getAbsolutePath());
                        String finalPath = defaultPath.getAbsolutePath() + File.separator;
                        String uriPath = uri.getPath();
                        if ((uriPath != null) && (uriPath.length() > 0)) {
                            Log.d("SETTINGS", "uri path = " + uriPath);
                            if (uriPath.startsWith("/tree/home:")) {
                                String[] paths = uriPath.split(":");
                                if (paths.length == 2)
                                    finalPath += "Documents" + File.separator + paths[1];
                                else
                                    finalPath += "Documents";
                            } else if (uriPath.startsWith("/tree/primary:")) {
                                String[] paths = uriPath.split(":");
                                if (paths.length == 2)
                                    finalPath += paths[1];
                            }
                        }
                        Log.d("SETTINGS", "Final path = " + finalPath);
                        TextView textView = findViewById(R.id.EditTextRootLocation);
                        textView.setText(finalPath);
                    }
                }
            }
        }
    }

    private void getXmlFiles(Uri uri) {
        Log.d("SETTINGS", "uri = " + uri.getPath());
        DocumentFile dir = DocumentFile.fromTreeUri(this, uri);
        Log.d("SETTINGS", "isDirectory = " + dir.isDirectory());
        Log.d("SETTINGS", "dir getName = " + dir.getName());
        DocumentFile[] tmpFiles = dir.listFiles();
        for (DocumentFile tmpFile : tmpFiles) {
            Log.d("SETTINGS", "tmpFile getUri = " + tmpFile.getUri());
            Log.d("SETTINGS", "tmpFile getUri.getPath = " + tmpFile.getUri().getPath());
            Log.d("SETTINGS", "tmpFile getUri.getPathSegments = " + tmpFile.getUri().getPathSegments());
            Log.d("SETTINGS", "tmpFile getName = " + tmpFile.getName());
            Log.d("SETTINGS", "tmpFile getParentfile.getName = " + tmpFile.getParentFile().getName());
            if (tmpFile.isDirectory()) {
                getXmlFiles(tmpFile.getUri());
            } else {
                DocumentFile tmp = DocumentFile.fromSingleUri(this, tmpFile.getUri());
                Log.d("SETTINGS", "SingleURI getName = " + tmp.getName());
            }
        }
    }
}
