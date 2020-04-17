package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.adapter.MainListAdapter;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class MainList extends Activity {
    public static int selectedItemPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayHomeAsUpEnabled(false);
            actionbar.setDisplayShowHomeEnabled(true);
        }
        loadDatabaseInList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.close_btn:
                intent = new Intent(this, Home.class);
                startActivity(intent);
                return true;

            case R.id.search_btn:
                intent = new Intent(this, SearchInDatabase.class);
                startActivity(intent);
                return true;

            case R.id.menu_about:
                intent = new Intent(this, DatabaseDetails.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadDatabaseInList() {
        ArrayList<DbItem> items = EncycloDatabase.getInstance().getItemsList();
        if (items != null) {
            ListView lv = findViewById(R.id.listView1);

            // Get preferences
            SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
            // Get Scrollbar position
            int scrollbarPosition = pref.getInt(Settings.KEY_SCROLLBAR, 0);
            // Set position according to settings
            if (scrollbarPosition == 1)
                lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
            else
                lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);

            int nbSelected = 0, j;
            for (DbItem item : items) {
                if (item.isSelected())
                    nbSelected++;
            }

            if (nbSelected > 0) {
                String[] lv_arr = new String[nbSelected];
                j = 0;
                for (DbItem item : items) {
                    if (item.isSelected()) {
                        lv_arr[j] = item.toString();
                        item.setListPosition(j);
                        j++;
                    } else
                        item.setListPosition(-1);
                }

                final int maxPosition = lv_arr.length - 1;

                lv.setAdapter(new MainListAdapter(getApplicationContext(), Arrays.asList(lv_arr)));
                lv.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedItemPosition = position;

                        Intent intent = new Intent(MainList.this, DisplayItem.class);
                        intent.putExtra("position", position);
                        intent.putExtra("maxPosition", maxPosition);
                        startActivity(intent);
                    }
                });

                lv.setSelectionFromTop(selectedItemPosition, 15);
            }
        }
    }
}