package com.avlweb.collectionviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;

import com.avlweb.collectionviewer.R;
import com.avlweb.collectionviewer.adapter.MainListAdapter;
import com.avlweb.collectionviewer.model.CollectionItem;
import com.avlweb.collectionviewer.model.CollectionModel;
import com.avlweb.collectionviewer.util.XmlFactory;

import java.util.ArrayList;
import java.util.List;

public class MainList extends BaseActivity implements MainListAdapter.customButtonListener {
    private final int ACTIVITY_ITEM_MODIFY = 486758485;
    private final int ACTIVITY_ITEM_DISPLAY = 846516548;
    private int position = 0;
    private int maxPosition = 0;
    private MainListAdapter adapter;
    private boolean collectionIsModified = false;

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

        displayHelpButton();
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
            case (android.R.id.home):
                // Save all changes to XML file if needed
                if (collectionIsModified) {
                    if (XmlFactory.writeXml())
                        Toast.makeText(getApplicationContext(), R.string.successfully_saved, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), R.string.problem_during_save, Toast.LENGTH_LONG).show();
                }
                // Go back to Home
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case (R.id.add_btn):
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_new_something);
                dialog.setTitle(getString(R.string.new_item));

                TextView textView = dialog.findViewById(R.id.message);
                textView.setText(R.string.message_new_item);

                Button btnOK = dialog.findViewById(R.id.btn_ok);
                Button btnCancel = dialog.findViewById(R.id.btn_cancel);
                btnOK.setOnClickListener(view -> {
                    EditText propertyName = dialog.findViewById(R.id.propertyName);
                    String name = propertyName.getText().toString();
                    if (name.isEmpty()) {
                        propertyName.setError(getString(R.string.must_not_be_empty));
                        return;
                    }
                    dialog.dismiss();
                    addItem(name);
                });
                btnCancel.setOnClickListener(view -> {
                    hideKeyboard();
                    dialog.cancel();
                });

                dialog.setCancelable(false);
                dialog.show();
                return true;

            case (R.id.search_btn):
                intent = new Intent(this, SearchInCollection.class);
                startActivity(intent);
                return true;

            case (R.id.menu_about):
                intent = new Intent(this, CollectionDetails.class);
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
            case (R.id.menu_modify):
                collectionIsModified = true;
                Intent intent = new Intent(this, ItemModify.class);
                intent.putExtra("position", position);
                startActivityForResult(intent, ACTIVITY_ITEM_MODIFY);
                return true;
            case (R.id.menu_delete):
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.warning);
                alertDialogBuilder.setIcon(R.drawable.ic_warning);
                alertDialogBuilder.setMessage(R.string.warning_item_deletion);
                alertDialogBuilder.setNegativeButton(getString(R.string.no),
                        (dialog, arg1) -> dialog.cancel());
                alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                        (arg0, arg1) -> deleteItem());
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
                ListView lv = findViewById(R.id.listViewMain);
                lv.setSelectionFromTop(this.position, 30);
            }
        } else if ((requestCode == ACTIVITY_ITEM_MODIFY) && (resultCode == Activity.RESULT_OK)) {
            if (resultData != null) {
                this.position = resultData.getIntExtra("position", 0);
                ListView lv = findViewById(R.id.listViewMain);
                lv.setSelectionFromTop(this.position, 30);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void buildMainListContent() {
        ArrayList<CollectionItem> items = CollectionModel.getInstance().getItems();
        ArrayList<CollectionItem> selectedItems = new ArrayList<>();

        if ((items != null) && (items.size() > 0)) {
            int idx = 0;
            for (CollectionItem item : items) {
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
        ListView lv = findViewById(R.id.listViewMain);
        if (scrollbarPosition == 1)
            lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
        else
            lv.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);

        // Create listview adapter
        adapter = null;     // For GC !
        adapter = new MainListAdapter(this, selectedItems, scrollbarPosition);
        adapter.setCustomButtonListener(this);
        lv.setAdapter(adapter);
        lv.setSelectionFromTop(this.position, 30);
        registerForContextMenu(lv);

        TextView textView = findViewById(R.id.textViewTitle);
        textView.setText(CollectionModel.getInstance().getInfos().getName());

        hideOrNotListView();
    }

    private void hideOrNotListView() {
        if (adapter.getCount() == 0) {
            ListView lv = findViewById(R.id.listViewMain);
            lv.setVisibility(View.INVISIBLE);
            TextView textView = findViewById(R.id.textView);
            textView.setVisibility(View.VISIBLE);
        } else {
            ListView lv = findViewById(R.id.listViewMain);
            lv.setVisibility(View.VISIBLE);
            TextView textView = findViewById(R.id.textView);
            textView.setVisibility(View.INVISIBLE);
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

    private void addItem(String name) {
        CollectionModel collectionModel = CollectionModel.getInstance();
        // Create new item
        CollectionItem item = new CollectionItem();
        item.setName(name);
        item.setPositionInSelectedList(collectionModel.getNbItems());
        // Add item to collection
        collectionModel.addItem(item);
        collectionIsModified = true;
        // Add item to adapter
        adapter.add(item);
        // Hide or not listView
        hideOrNotListView();
        // Call modification page
        this.position = collectionModel.getNbItems() - 1;
        this.maxPosition++;
        Intent intent = new Intent(this, ItemModify.class);
        intent.putExtra("position", position);
        startActivityForResult(intent, ACTIVITY_ITEM_MODIFY);
    }

    private void deleteItem() {
        List<CollectionItem> items = CollectionModel.getInstance().getItems();
        // Delete item from adapter
        adapter.remove(items.get(this.position));
        // Hide or not listView
        hideOrNotListView();
        // Delete item from database
        items.remove(this.position);
        collectionIsModified = true;
        Toast.makeText(getApplicationContext(), R.string.item_deletion_successful, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }
}
