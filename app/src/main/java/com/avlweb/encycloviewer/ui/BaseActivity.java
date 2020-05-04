package com.avlweb.encycloviewer.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.avlweb.encycloviewer.R;

import static com.avlweb.encycloviewer.ui.Settings.KEY_HIDE_HELP_BUTTON;
import static com.avlweb.encycloviewer.ui.Settings.KEY_PREFS;

public abstract class BaseActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void displayHelpButton() {
        // Get preferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences(KEY_PREFS, MODE_PRIVATE);

        // Get flag "Hide help button"
        boolean hideHelpButton = pref.getBoolean(KEY_HIDE_HELP_BUTTON, false);
        ImageButton helpButton = findViewById(R.id.fab);
        helpButton.setVisibility(hideHelpButton ? View.INVISIBLE : View.VISIBLE);
    }

    public void openHelp(View view) {

        if (this instanceof Home) {
            Toast.makeText(getApplicationContext(), "openHelp by Home !", Toast.LENGTH_SHORT).show();
        } else if (this instanceof DatabaseModify) {
            Toast.makeText(getApplicationContext(), "openHelp by DatabaseModify !", Toast.LENGTH_SHORT).show();
        } else if (this instanceof ItemDisplay) {
            Toast.makeText(getApplicationContext(), "openHelp by ItemDisplay !", Toast.LENGTH_SHORT).show();
        } else if (this instanceof ItemModify) {
            Toast.makeText(getApplicationContext(), "openHelp by ItemModify !", Toast.LENGTH_SHORT).show();
        } else if (this instanceof MainList) {
            Toast.makeText(getApplicationContext(), "openHelp by MainList !", Toast.LENGTH_SHORT).show();
        }
    }
}
