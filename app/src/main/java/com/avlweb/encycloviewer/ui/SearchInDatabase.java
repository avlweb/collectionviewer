package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.model.FieldDescription;

import java.util.List;

public class SearchInDatabase extends Activity {
    private static String nameSearch = null;
    private static String descriptionSearch = null;
    private static String[] fieldSearch = null;
    private static int nbFields = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setTitle(getString(R.string.search_in_database));

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        EncycloDatabase database = EncycloDatabase.getInstance();
        if (database.getFieldDescriptions() != null) {
            LinearLayout linearLayout = findViewById(R.id.linearlayout);
            int idx = 0;
            // Check if it is not the first time we reach the search view
            if ((nbFields == 0) || (nbFields != database.getNbFields())) {
                nbFields = database.getNbFields();
                fieldSearch = null;
                fieldSearch = new String[nbFields];
                nameSearch = null;
                descriptionSearch = null;
            }
            // Create and fill fields
            for (FieldDescription field : database.getFieldDescriptions()) {
                TextView textView = new TextView(this);
                textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                textView.setText(field.getName());
                textView.setTextColor(getColor(R.color.black));
                textView.setPadding(20, 20, 20, 20);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setTypeface(null, Typeface.BOLD);
                linearLayout.addView(textView);

                EditText editText = new EditText(this);
                editText.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                editText.setHint(R.string.words_to_search);
                editText.setHintTextColor(getColor(R.color.dark_gray));
                editText.setGravity(Gravity.TOP);
                editText.setPadding(20, 0, 20, 10);
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                editText.setMinHeight(48);
                editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                editText.setSingleLine();
                editText.setId(field.getId());
                linearLayout.addView(editText);

                if (fieldSearch[idx] != null) editText.setText(fieldSearch[idx]);
                idx++;
            }
            // Fill name if already exists
            if ((nameSearch != null) && (nameSearch.length() > 0)) {
                EditText name = findViewById(R.id.textName);
                name.setText(nameSearch);
            }
            // Fill name if already exists
            if ((descriptionSearch != null) && (descriptionSearch.length() > 0)) {
                EditText description = findViewById(R.id.textDescription);
                description.setText(descriptionSearch);
            }
        }
    }

    public void searchInDatabase(View view) {
        String[][] stringsToSearch = new String[nbFields][];
        String[] namesToSearch = null;
        String[] descriptionsToSearch = null;
        EncycloDatabase database = EncycloDatabase.getInstance();
        int nbFieldsToMatch = 0;

        // Name
        EditText name = findViewById(R.id.textName);
        if ((name.getText() != null) && (name.getText().length() > 0)) {
            nameSearch = name.getText().toString();
            namesToSearch = nameSearch.split(",");
            nbFieldsToMatch++;
        }

        // Description
        EditText description = findViewById(R.id.textDescription);
        if ((description.getText() != null) && (description.getText().length() > 0)) {
            descriptionSearch = description.getText().toString();
            descriptionsToSearch = descriptionSearch.split(",");
            nbFieldsToMatch++;
        }

        // Fields
        int idx = 0;
        for (FieldDescription field : database.getFieldDescriptions()) {
            EditText editText = findViewById(field.getId());
            if ((editText.getText() != null) && (editText.getText().length() > 0)) {
                fieldSearch[idx] = editText.getText().toString();
                stringsToSearch[idx] = fieldSearch[idx].split(",");
                nbFieldsToMatch++;
            }
            idx++;
        }

        if (nbFieldsToMatch == 0)
            return;

        List<DbItem> items = database.getItemsList();
        if (items != null) {
            for (DbItem item : items)
                item.setNotSelected();

            int nbElementsFound = 0;
            for (DbItem item : items) {

                int nbFieldsMatching = 0;
                // Name
                if (namesToSearch != null) {
                    String element = item.getName().toLowerCase();
                    int nbStringsOk = 0;
                    for (String toSearch : namesToSearch) {
                        if (element.contains(toSearch))
                            nbStringsOk++;
                        else
                            break;
                    }
                    if (nbStringsOk == namesToSearch.length)
                        nbFieldsMatching++;
                }
                // Description
                if (descriptionsToSearch != null) {
                    String element = item.getDescription().toLowerCase();
                    int nbStringsOk = 0;
                    for (String toSearch : descriptionsToSearch) {
                        if (element.contains(toSearch))
                            nbStringsOk++;
                        else
                            break;
                    }
                    if (nbStringsOk == descriptionsToSearch.length)
                        nbFieldsMatching++;
                }
                // Field
                for (idx = 0; idx < nbFields; idx++) {
                    if (stringsToSearch[idx] == null)
                        continue;

                    String element = item.getField(idx);
                    if ((element == null) || (element.length() == 0))
                        continue;

                    element = element.toLowerCase();
                    int nbStringsOk = 0;
                    for (int j = 0; j < stringsToSearch[idx].length; j++) {
                        if (element.contains(stringsToSearch[idx][j]))
                            nbStringsOk++;
                        else
                            break;
                    }

                    if (nbStringsOk == stringsToSearch[idx].length)
                        nbFieldsMatching++;
                    else
                        break;
                }

                if (nbFieldsMatching == nbFieldsToMatch) {
                    item.setSelected();
                    nbElementsFound++;
                }
            }

            if (nbElementsFound == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.search);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage(getString(R.string.no_element_found));
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();

                for (DbItem item : items)
                    item.setSelected();
            } else
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.found_elements), nbElementsFound), Toast.LENGTH_SHORT).show();
        }
    }

    public void clearSearch(View view) {
        // It's time to clean
        List<DbItem> items = EncycloDatabase.getInstance().getItemsList();
        for (DbItem item : items)
            item.setSelected();
        // Name
        EditText editText = findViewById(R.id.textName);
        editText.setText("");
        nameSearch = null;
        // Description
        editText = findViewById(R.id.textDescription);
        editText.setText("");
        descriptionSearch = null;
        // Fields
        int idx = 0;
        for (FieldDescription field : EncycloDatabase.getInstance().getFieldDescriptions()) {
            editText = findViewById(field.getId());
            editText.setText("");
            fieldSearch[idx] = null;
            idx++;
        }
        fieldSearch = null;
        Toast.makeText(getApplicationContext(), R.string.clear_done, Toast.LENGTH_SHORT).show();
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
