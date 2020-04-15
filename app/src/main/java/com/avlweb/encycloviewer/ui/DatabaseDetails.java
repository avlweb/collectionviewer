package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.core.app.NavUtils;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.FieldDescription;
import com.avlweb.encycloviewer.util.xmlFactory;

import java.util.ArrayList;
import java.util.HashMap;
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

        TextView textView = findViewById(R.id.textName);
        textView.setText(MainList.dbInfos.getName());

        textView = findViewById(R.id.textDescription);
        textView.setText(MainList.dbInfos.getDescription());

        textView = findViewById(R.id.textVersion);
        textView.setText(MainList.dbInfos.getVersion());

        Intent intent = getIntent();
        int nbItems = intent.getIntExtra("nbItems", 0);
        textView = findViewById(R.id.textNbItems);
        textView.setText(Integer.toString(nbItems));

//        ListView lv = findViewById(R.id.fieldsList);
//        ArrayList<Map<String, String>> list = buildData();
//        String[] from = {"name", "description"};
//        int[] to = {android.R.id.text1, android.R.id.text2};
//        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.my_simple_list_2, from, to);
//        lv.setAdapter(adapter);

        LinearLayout linearLayout = findViewById(R.id.linearlayout);
        for (FieldDescription field : MainList.dbInfos.getFieldDescriptions()) {
            textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setText(field.getName());
            textView.setTextColor(getColor(R.color.black));
            textView.setPadding(20, 20, 20, 20);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setTypeface(null, Typeface.BOLD);
            linearLayout.addView(textView);

            EditText editText = new EditText(this);
            editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            editText.setHint(field.getDescription());
            editText.setHintTextColor(getColor(R.color.dark_gray));
            editText.setGravity(Gravity.TOP);
            editText.setText(field.getDescription());
            editText.setPadding(20, 0, 20, 10);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            editText.setMinHeight(48);
            editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            editText.setSingleLine();
            editText.setId(field.getId());
            linearLayout.addView(editText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_database_details, menu);
        return true;
    }

    private ArrayList<Map<String, String>> buildData() {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        for (FieldDescription field : MainList.dbInfos.getFieldDescriptions()) {
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
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.save_btn:
                writeFile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean writeFile() {
        xmlFactory.writeXml();
        return true;
    }
}
