package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.core.app.NavUtils;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DatabaseInfos;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.model.FieldDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DatabaseDetails extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_database_details);

        setTitle(getString(R.string.about_database));

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        DatabaseInfos dbInfos = EncycloDatabase.getInstance().getInfos();
        TextView textView = findViewById(R.id.textName);
        textView.setText(dbInfos.getName());

        textView = findViewById(R.id.textDescription);
        textView.setText(dbInfos.getDescription());

        textView = findViewById(R.id.textVersion);
        textView.setText(dbInfos.getVersion());

        textView = findViewById(R.id.textNbItems);
        textView.setText(String.format(Locale.getDefault(), "%d", EncycloDatabase.getInstance().getNbItems()));

        ListView lv = findViewById(R.id.fieldsList);
        if (EncycloDatabase.getInstance().getNbFields() > 0) {
            ArrayList<Map<String, String>> list = buildData();
            String[] from = {"name", "description"};
            int[] to = {android.R.id.text1, android.R.id.text2};
            SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.my_simple_list_2, from, to);
            lv.setAdapter(adapter);
        } else {
            textView = findViewById(R.id.textView);
            textView.setVisibility(View.VISIBLE);
            lv.setVisibility(View.GONE);
        }
    }

    private ArrayList<Map<String, String>> buildData() {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        for (FieldDescription field : EncycloDatabase.getInstance().getFieldDescriptions()) {
            list.add(putData(field));
        }
        return list;
    }

    private HashMap<String, String> putData(FieldDescription field) {
        HashMap<String, String> item = new HashMap<>();
        item.put("name", field.getName());
        item.put("description", field.getDescription());
        return item;
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
