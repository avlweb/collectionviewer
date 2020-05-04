package com.avlweb.encycloviewer.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.avlweb.encycloviewer.R;

import static com.avlweb.encycloviewer.ui.Settings.KEY_HIDE_HELP_BUTTON;
import static com.avlweb.encycloviewer.ui.Settings.KEY_PREFS;

public abstract class BaseActivity extends Activity {
    private final int ACTIVITY_DISPLAY_HELP = 864316548;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == ACTIVITY_DISPLAY_HELP) {
            if (resultData != null) {
            }
        }
    }

    public void openHelp(View view) {
        // Open help at the right position
        Intent intent = new Intent(this, Help.class);
        if (this instanceof Home) {
            intent.putExtra("origin", Help.HELP_HOME);
        } else if (this instanceof DatabaseModify) {
            intent.putExtra("origin", Help.HELP_DATABASE_MODIFY);
        } else if (this instanceof ItemDisplay) {
            intent.putExtra("origin", Help.HELP_ITEM_DISPLAY);
        } else if (this instanceof ItemModify) {
            intent.putExtra("origin", Help.HELP_ITEM_MODIFY);
        } else if (this instanceof MainList) {
            intent.putExtra("origin", Help.HELP_MAINLIST);
        }
        startActivityForResult(intent, ACTIVITY_DISPLAY_HELP);
    }
}
