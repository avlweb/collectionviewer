package com.avlweb.encycloviewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity {

    public static final String KEY_PREFS = "EncycloViewerPreferences";
    public static final String KEY_DATABASES_ROOT_LOCATION = "key_language";
    public static final String KEY_HIDE_SAMPLE_DATABASE = "key_hide_sample";
    public static final String KEY_SCROLLBAR = "key_scrollbar";

    String[] scrollbarPositions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false);
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
        String databasesRootLocation = pref.getString(KEY_DATABASES_ROOT_LOCATION, this.getExternalFilesDir(null).toString());
        TextView textView = findViewById(R.id.EditTextRootLocation);
        textView.setText(databasesRootLocation);
        // Get Scrollbar position
        int scrollbar = pref.getInt(KEY_SCROLLBAR, 0);
        scrollbarPositions = getResources().getStringArray(R.array.scrollbar_position_array);
        Spinner spinner = findViewById(R.id.spinnerScrollbar);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.scrollbar_position_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(scrollbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
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
        // Hide sample database
        Switch hide = findViewById(R.id.switch_hide);
        editor.putBoolean(KEY_HIDE_SAMPLE_DATABASE, hide.isChecked());
        // Save preferences
        editor.apply();
        Toast.makeText(getApplicationContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
