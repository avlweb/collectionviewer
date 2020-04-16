package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;
import androidx.documentfile.provider.DocumentFile;

import com.avlweb.encycloviewer.R;

import java.io.File;
import java.util.ArrayList;

public class Settings extends Activity {

    public static final String KEY_PREFS = "EncycloViewerPreferences";
    public static final String KEY_DATABASES_ROOT_LOCATION = "key_language";
    public static final String KEY_HIDE_SAMPLE_DATABASE = "key_hide_sample";
    public static final String KEY_SCROLLBAR = "key_scrollbar";
    public static final String KEY_FONT_SIZE = "key_font_size";

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
        // Get flag "Hide sample database"
        boolean hideSampledatabase = pref.getBoolean(KEY_HIDE_SAMPLE_DATABASE, false);
        Switch hide = findViewById(R.id.switch_hide);
        hide.setChecked(hideSampledatabase);
        // Get Databases Root location
        File defaultPath = this.getExternalFilesDir(null);
        if (defaultPath != null) {
            String databasesRootLocation = pref.getString(KEY_DATABASES_ROOT_LOCATION, defaultPath.toString());
            TextView textView = findViewById(R.id.EditTextRootLocation);
            textView.setText(databasesRootLocation);
        }
        // Get Scrollbar position
        int scrollbar = pref.getInt(KEY_SCROLLBAR, 0);
        Spinner spinner = findViewById(R.id.spinnerScrollbar);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.scrollbar_position_array, R.layout.my_main_list);
        adapter.setDropDownViewResource(R.layout.my_main_list);
        spinner.setAdapter(adapter);
        spinner.setSelection(scrollbar);
        // Font size
        fontSize = pref.getInt(KEY_FONT_SIZE, 0);
        TextView textSeekbar = findViewById(R.id.TextLorum);
        textSeekbar.setTextSize(getFontSizeFromPref(fontSize));
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                doOnFontSizeChanged(group, checkedId);
            }
        });
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

    private void doOnFontSizeChanged(RadioGroup group, int checkedId) {
        int checkedRadioId = group.getCheckedRadioButtonId();
        switch (checkedRadioId) {
            case R.id.radioButtonSmall:
                fontSize = 0;
                break;
            case R.id.radioButtonNormal:
                fontSize = 1;
                break;
            case R.id.radioButtonBig:
                fontSize = 2;
                break;
            case R.id.radioButtonVerybig:
                fontSize = 3;
                break;
        }
        TextView textSeekbar = findViewById(R.id.TextLorum);
        textSeekbar.setTextSize(getFontSizeFromPref(fontSize));
    }

    public static int getFontSizeFromPref(int val) {
        int res = 14;
        switch (val) {
            case 0:
                res = 14;
                break;
            case 1:
                res = 16;
                break;
            case 2:
                res = 18;
                break;
            case 3:
                res = 20;
                break;
        }
        return res;
    }

    public void SaveSettings(View view) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        // Databases Root location
        EditText rootLocation = findViewById(R.id.EditTextRootLocation);
        editor.putString(KEY_DATABASES_ROOT_LOCATION, rootLocation.getText().toString());
        // Scrollbar position
        Spinner spinner = findViewById(R.id.spinnerScrollbar);
        editor.putInt(KEY_SCROLLBAR, spinner.getSelectedItemPosition());
        // Font size
        editor.putInt(KEY_FONT_SIZE, fontSize);
        // Hide sample database
        Switch hide = findViewById(R.id.switch_hide);
        editor.putBoolean(KEY_HIDE_SAMPLE_DATABASE, hide.isChecked());
        // Save preferences
        editor.apply();
        Toast.makeText(getApplicationContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void SearchFolder(View view) {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Provide read access to files and sub-directories in the user-selected directory.
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(intent, 102541);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == 102541 && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the directory that the user selected.
            if (resultData != null) {
                Uri uri = resultData.getData();
                if (uri != null) {
                    File defaultPath = Environment.getExternalStorageDirectory();
//                    Log.d("SETTINGS", "getExternalStorageDirectory() = " + Environment.getExternalStorageDirectory());
//                    Log.d("SETTINGS", "getDataDirectory() = " + Environment.getDataDirectory());
//                    Log.d("SETTINGS", "getExternalMediaDirs() = " + Arrays.toString(this.getExternalMediaDirs()));
//                    Log.d("SETTINGS", "getExternalFilesDirs() = " + Arrays.toString(this.getExternalFilesDirs(android.os.Environment.DIRECTORY_DOCUMENTS)));
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
                        TextView textView = findViewById(R.id.EditTextRootLocation);
                        textView.setText(finalPath);
                    }
                }
            }
        }
    }

    private void getXmlFiles(ArrayList<String> files, Uri uri) {
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
//            DocumentFile tmp = DocumentFile.fromSingleUri(this, tmpFile.getUri());
//            if (tmpFile.isDirectory())
//                getXmlFiles(files, tmpFile.getUri());
//            else {
//                if (tmpFile.getName().endsWith(".xml")) {
//                    if (tmpFile.getName().endsWith("Sample_database.xml"))
//                        continue;
//                    files.add(tmpFile.getName());
//                }
//            }
        }
    }
}
