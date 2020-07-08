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
import com.avlweb.encycloviewer.model.Property;

import java.util.List;
import java.util.Locale;

public class SearchInDatabase extends Activity {
    private static String originalNameToSearch = null;
    private static String originalDescriptionToSearch = null;
    private static String[] originalPropertiesToSearch = null;
    private static int nbProperties = 0;
    private static String dbName = null;

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
        List<Property> properties = database.getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            LinearLayout linearLayout = findViewById(R.id.linearlayout);
            int idx = 0;
            // Check if it is not the first time we reach the search view for this database
            if ((nbProperties == 0) || (nbProperties != database.getNbProperties()) || (!dbName.equals(database.getInfos().getName()))) {
                // Save data about current database
                dbName = database.getInfos().getName();
                nbProperties = database.getNbProperties();
                originalPropertiesToSearch = null;
                originalPropertiesToSearch = new String[nbProperties];
                originalNameToSearch = null;
                originalDescriptionToSearch = null;
            }
            // Create and fill properties
            for (Property property : properties) {
                TextView textView = new TextView(this);
                textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                textView.setText(property.getName());
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
                editText.setId(property.getId());
                linearLayout.addView(editText);

                if (originalPropertiesToSearch[idx] != null)
                    editText.setText(originalPropertiesToSearch[idx]);
                idx++;
            }
            // Fill name if already exists
            if ((originalNameToSearch != null) && (originalNameToSearch.length() > 0)) {
                EditText name = findViewById(R.id.textName);
                name.setText(originalNameToSearch);
            }
            // Fill name if already exists
            if ((originalDescriptionToSearch != null) && (originalDescriptionToSearch.length() > 0)) {
                EditText description = findViewById(R.id.textDescription);
                description.setText(originalDescriptionToSearch);
            }
        }
    }

    public void searchInDatabase(View view) {
        String[][] stringsToSearch = new String[nbProperties][];
        String[] namesToSearch = null;
        String[] descriptionsToSearch = null;
        EncycloDatabase database = EncycloDatabase.getInstance();
        int nbStringsToMatch = 0;

        // Name
        EditText name = findViewById(R.id.textName);
        if ((name.getText() != null) && (name.getText().length() > 0)) {
            originalNameToSearch = name.getText().toString();
            namesToSearch = originalNameToSearch.split(",");
            nbStringsToMatch++;
        }

        // Description
        EditText description = findViewById(R.id.textDescription);
        if ((description.getText() != null) && (description.getText().length() > 0)) {
            originalDescriptionToSearch = description.getText().toString();
            descriptionsToSearch = originalDescriptionToSearch.split(",");
            nbStringsToMatch++;
        }

        // Properties
        int idx = 0;
        List<Property> properties = EncycloDatabase.getInstance().getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            for (Property property : properties) {
                EditText editText = findViewById(property.getId());
                if ((editText.getText() != null) && (editText.getText().length() > 0)) {
                    originalPropertiesToSearch[idx] = editText.getText().toString();
                    stringsToSearch[idx] = originalPropertiesToSearch[idx].split(",");
                    nbStringsToMatch++;
                }
                idx++;
            }
        }

        if (nbStringsToMatch == 0)
            return;

        List<DbItem> items = database.getItems();
        if ((items != null) && (items.size() > 0)) {
            for (DbItem item : items)
                item.setNotSelected();

            int nbElementsFound = 0;
            for (DbItem item : items) {
                int nbStringsMatching = 0;

                // Name
                if (namesToSearch != null) {
                    if (item.getName() != null) {
                        String element = item.getName().toLowerCase();
                        int nbStringsOk = 0;
                        for (String toSearch : namesToSearch) {
                            if (element.contains(toSearch))
                                nbStringsOk++;
                            else
                                break;
                        }
                        if (nbStringsOk == namesToSearch.length)
                            nbStringsMatching++;
                    }
                }
                // Description
                if (descriptionsToSearch != null) {
                    if (item.getDescription() != null) {
                        String element = item.getDescription().toLowerCase();
                        int nbStringsOk = 0;
                        for (String toSearch : descriptionsToSearch) {
                            if (element.contains(toSearch))
                                nbStringsOk++;
                            else
                                break;
                        }
                        if (nbStringsOk == descriptionsToSearch.length)
                            nbStringsMatching++;
                    }
                }
                // Properties
                for (idx = 0; idx < nbProperties; idx++) {
                    if (stringsToSearch[idx] == null)
                        continue;

                    String element = item.getProperty(idx);
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
                        nbStringsMatching++;
                    else
                        break;
                }

                if (nbStringsMatching == nbStringsToMatch) {
                    item.setSelected();
                    nbElementsFound++;
                }
            }

            if (nbElementsFound == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.search);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage(getString(R.string.no_item_found));
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();

                for (DbItem item : items)
                    item.setSelected();
            } else
                Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(), getString(R.string.found_items), nbElementsFound), Toast.LENGTH_SHORT).show();
        }
    }

    public void clearSearch(View view) {
        // It's time to clean
        List<DbItem> items = EncycloDatabase.getInstance().getItems();
        if ((items != null) && (items.size() > 0)) {
            for (DbItem item : items)
                item.setSelected();
        }
        // Name
        EditText editText = findViewById(R.id.textName);
        editText.setText("");
        originalNameToSearch = null;
        // Description
        editText = findViewById(R.id.textDescription);
        editText.setText("");
        originalDescriptionToSearch = null;
        // Properties
        int idx = 0;
        List<Property> properties = EncycloDatabase.getInstance().getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            for (Property property : properties) {
                editText = findViewById(property.getId());
                editText.setText("");
                if (originalPropertiesToSearch != null)
                    originalPropertiesToSearch[idx] = null;
                idx++;
            }
        }
        originalPropertiesToSearch = null;
        nbProperties = 0;
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
