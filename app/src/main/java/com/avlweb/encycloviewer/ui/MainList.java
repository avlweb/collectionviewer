package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.adapter.MainListAdapter;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.util.xmlFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainList extends Activity implements MainListAdapter.customButtonListener {
    private int position;
    private int maxPosition;
    private MainListAdapter adapter;

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

            case R.id.add_btn:
                createNewItem();
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
                Intent intent = new Intent(this, ItemModify.class);
                intent.putExtra("position", position);
                startActivityForResult(intent, 48484848);
                return true;
            case R.id.menu_delete:
                deleteItem();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (((requestCode == 48484848) || (requestCode == 846516548)) && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                int newPosition = resultData.getIntExtra("position", 0);
                this.position = newPosition;
                ListView lv = findViewById(R.id.listView1);
                lv.setSelectionFromTop(this.position, 30);
            }
        }
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

                this.maxPosition = lv_arr.length - 1;

                adapter = new MainListAdapter(this, Arrays.asList(lv_arr));
                adapter.setCustomButtonListener(this);
                lv.setAdapter(adapter);
                lv.setSelectionFromTop(this.position, 30);
                registerForContextMenu(lv);
            }
        }
    }

    @Override
    public void onButtonClickListener(View view, int position, String value) {
        this.position = position;
        openContextMenu(view);
    }

    @Override
    public void onTextClickListener(int position, String value) {
        this.position = position;
        Intent intent = new Intent(this, ItemDisplay.class);
        intent.putExtra("position", position);
        intent.putExtra("maxPosition", this.maxPosition);
        startActivityForResult(intent, 846516548);
    }

    private void createNewItem() {
        // Create new item
        DbItem item = new DbItem();
        EncycloDatabase database = EncycloDatabase.getInstance();
        for (int idx = 1; idx <= database.getNbFields(); idx++)
            item.addField("New content " + idx);
        item.addImagePath("images/image1.jpg");
        item.setListPosition(database.getNbItems());
        database.addItemToList(item);
        // Write XML file
        xmlFactory.writeXml();
        // Call modification page
        this.position = database.getNbItems() - 1;
        Intent intent = new Intent(this, ItemModify.class);
        intent.putExtra("position", position);
        startActivityForResult(intent, 846516548);
    }

    private void deleteItem() {
        List<DbItem> items = EncycloDatabase.getInstance().getItemsList();
        items.remove(this.position);
    }
}
