package com.avlweb.encycloviewer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;

public class Settings extends Activity {

    public static final String KEY_PREFS = "EncycloViewerPreferences";
    public static final String KEY_DATABASES_ROOT_LOCATION = "key_language";
    public static final String KEY_HIDE_SAMPLE_DATABASE = "key_hide_sample";
    public static final String KEY_SCROLLBAR = "key_scrollbar";
    public static final String KEY_FONT_SIZE = "key_font_size";

    private String[] scrollbarPositions;
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
        String databasesRootLocation = pref.getString(KEY_DATABASES_ROOT_LOCATION, this.getExternalFilesDir(null).toString());
        TextView textView = findViewById(R.id.EditTextRootLocation);
        textView.setText(databasesRootLocation);
        // Get Scrollbar position
        int scrollbar = pref.getInt(KEY_SCROLLBAR, 0);
        scrollbarPositions = getResources().getStringArray(R.array.scrollbar_position_array);
        Spinner spinner = findViewById(R.id.spinnerScrollbar);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.scrollbar_position_array, R.layout.mainlist);
        adapter.setDropDownViewResource(R.layout.mainlist);
        spinner.setAdapter(adapter);
        spinner.setSelection(scrollbar);
        // Font size
        fontSize = pref.getInt(KEY_FONT_SIZE, 0);
        TextView textSeekbar = findViewById(R.id.TextSeekbar);
        textSeekbar.setTextSize(getFontSizeFromPref(fontSize));
        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setProgress(fontSize);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                Log.d("Seetings", "seekbar : value = " + progress);
                if (progress != fontSize) {
                    TextView textView = findViewById(R.id.TextSeekbar);
                    textView.setTextSize(getFontSizeFromPref(progress));
                    fontSize = progress;
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("Seetings", "Start bar progress is :" + progressChangedValue);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("Seetings", "Seek bar progress is :" + progressChangedValue);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
    }

    private int getFontSizeFromPref(int val) {
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
}
