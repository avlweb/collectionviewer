package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.adapter.MainListAdapter;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.util.xmlFactory;

import java.util.ArrayList;
import java.util.List;

public class MainList extends Activity implements MainListAdapter.customButtonListener {
    private final int ACTIVITY_ITEM_MODIFY = 486758485;
    private final int ACTIVITY_ITEM_DISPLAY = 846516548;
    private int position = 0;
    private int maxPosition = 0;
    private MainListAdapter adapter;
    private boolean databaseModified = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_list);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        buildMainListContent();
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
            case android.R.id.home:
                // Save all changes to XML file if needed
                if (databaseModified) {
                    if (xmlFactory.writeXml())
                        Toast.makeText(getApplicationContext(), R.string.successfully_saved, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), R.string.problem_during_save, Toast.LENGTH_LONG).show();
                }
                // Go back to Home
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.add_btn:
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialog = inflater.inflate(R.layout.dialog_new_something, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(getString(R.string.new_item));
                alertDialog.setMessage(getString(R.string.message_new_item));
                alertDialog.setCancelable(false);
                final EditText propertyName = dialog.findViewById(R.id.propertyName);

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideKeyboard();
                        String name = propertyName.getText().toString();
                        if (name.length() > 0) {
                            createNewItem(name);
                        }
                    }
                });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setView(dialog);
                alertDialog.show();
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
                databaseModified = true;
                Intent intent = new Intent(this, ItemModify.class);
                intent.putExtra("position", position);
                startActivityForResult(intent, ACTIVITY_ITEM_MODIFY);
                return true;
            case R.id.menu_delete:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.warning);
                alertDialogBuilder.setIcon(R.drawable.ic_warning);
                alertDialogBuilder.setMessage(R.string.warning_item_deletion);
                alertDialogBuilder.setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                deleteItem();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if ((requestCode == ACTIVITY_ITEM_DISPLAY) && (resultCode == Activity.RESULT_OK)) {
            if (resultData != null) {
                this.position = resultData.getIntExtra("position", 0);
                ListView lv = findViewById(R.id.listView1);
                lv.setSelectionFromTop(this.position, 30);
            }
        } else if ((requestCode == ACTIVITY_ITEM_MODIFY) && (resultCode == Activity.RESULT_OK)) {
            if (resultData != null) {
                this.position = resultData.getIntExtra("position", 0);
                ListView lv = findViewById(R.id.listView1);
                lv.setSelectionFromTop(this.position, 30);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void buildMainListContent() {
        ArrayList<DbItem> items = EncycloDatabase.getInstance().getItems();
        ArrayList<DbItem> selectedItems = new ArrayList<>();

        if (items != null) {
            int idx = 0;
            for (DbItem item : items) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                    item.setPositionInSelectedList(idx);
                    idx++;
                } else
                    item.setPositionInSelectedList(-1);
            }

            if (selectedItems.size() > 0) {
                this.maxPosition = selectedItems.size() - 1;
            }
        }

        // Set scrollbar position according to settings
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
        int scrollbarPosition = pref.getInt(Settings.KEY_SCROLLBAR, 0);
        ListView lv = findViewById(R.id.listView1);
        if (scrollbarPosition == 1)
            lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
        else
            lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);

        // Create listview adapter
        adapter = null;     // For desallocation !
        adapter = new MainListAdapter(this, selectedItems);
        adapter.setCustomButtonListener(this);
        lv.setAdapter(adapter);
        lv.setSelectionFromTop(this.position, 30);
        registerForContextMenu(lv);

        hideOrNotListView();
    }

    private void hideOrNotListView() {
        if (adapter.getCount() == 0) {
            ListView lv = findViewById(R.id.listView1);
            lv.setVisibility(View.GONE);
            TextView textView = findViewById(R.id.textView);
            textView.setVisibility(View.VISIBLE);
        } else {
            ListView lv = findViewById(R.id.listView1);
            lv.setVisibility(View.VISIBLE);
            TextView textView = findViewById(R.id.textView);
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onButtonClickListener(View view, int position) {
        this.position = position;
        openContextMenu(view);
    }

    @Override
    public void onTextClickListener(int position) {
        this.position = position;
        Intent intent = new Intent(this, ItemDisplay.class);
        intent.putExtra("position", position);
        intent.putExtra("maxPosition", this.maxPosition);
        startActivityForResult(intent, ACTIVITY_ITEM_DISPLAY);
    }

    private void createNewItem(String name) {
        EncycloDatabase database = EncycloDatabase.getInstance();
        // Create new item
        DbItem item = new DbItem();
        item.setName(name);
        item.setPositionInSelectedList(database.getNbItems());
        // Add item to database
        database.addItem(item);
        databaseModified = true;
        // Add item to adapter
        adapter.add(item);
        // Hide or not listView
        hideOrNotListView();
        // Call modification page
        this.position = database.getNbItems() - 1;
        Intent intent = new Intent(this, ItemModify.class);
        intent.putExtra("position", position);
        startActivityForResult(intent, ACTIVITY_ITEM_MODIFY);
    }

    private void deleteItem() {
        List<DbItem> items = EncycloDatabase.getInstance().getItems();
        // Delete item from adapter
        adapter.remove(items.get(this.position));
        // Hide or not listView
        hideOrNotListView();
        // Delete item from database
        items.remove(this.position);
        databaseModified = true;
        Toast.makeText(getApplicationContext(), R.string.item_deletion_successful, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }
}
