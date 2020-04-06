package com.avlweb.encycloviewer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainList extends Activity {
    public static ArrayList<DbItem> itemsList = null;
    public static String dbpath = null;
    public static int selectedItemPosition = -1;
    public static int maxPosition = 0;
    public static DatabaseInfos dbInfos = null;
    public static int imgIdx = 0;

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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_list, menu);
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
                if (dbInfos != null) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setIcon(R.drawable.ic_launcher);
                    builder.setTitle(getString(R.string.about_database));
                    builder.setMessage(getString(R.string.name_points) + dbInfos.getName()
                            + "\n" +
                            getString(R.string.description_points) + dbInfos.getDescription()
                            + "\n" +
                            getString(R.string.version_points) + dbInfos.getVersion()
                            + "\n" +
                            getString(R.string.number_of_items) + itemsList.size());
                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadDatabaseInList() {
        if (dbpath != null) {
            if (itemsList != null) {
                DbItem element;
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

                int nbSelected = 0, i, j;
                for (i = 0; i < itemsList.size(); i++) {
                    element = itemsList.get(i);
                    if (element.isSelected())
                        nbSelected++;
                }

                if (nbSelected > 0) {
                    String[] lv_arr = new String[nbSelected];
                    j = 0;
                    for (i = 0; i < itemsList.size(); i++) {
                        element = itemsList.get(i);
                        if (element.isSelected()) {
                            lv_arr[j] = element.toString();
                            element.setListPosition(j);
                            j++;
                        } else
                            element.setListPosition(-1);
                    }

                    maxPosition = lv_arr.length - 1;

                    List<String> items = Arrays.asList(lv_arr);
                    lv.setAdapter(new MainListAdapter(getApplicationContext(), items));
                    lv.setOnItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedItemPosition = position;

                            Intent intent = new Intent(MainList.this, DisplayItem.class);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        }
                    });

                    lv.setSelectionFromTop(selectedItemPosition, 15);
                }
            }
        }
    }
}