package com.avlweb.collectionviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.NavUtils;
import com.avlweb.collectionviewer.model.Collection;
import com.avlweb.collectionviewer.model.CollectionInfos;
import com.avlweb.collectionviewer.model.Property;
import com.avlweb.collectionviewer.util.xmlFactory;
import com.avlweb.encycloviewer.R;

import java.util.List;

public class CollectionModify extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_database_modify);

        setTitle(getString(R.string.collection_modify));

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        CollectionInfos dbInfos = Collection.getInstance().getInfos();
        TextView textView = findViewById(R.id.textName);
        textView.setText(dbInfos.getName());

        textView = findViewById(R.id.textDescription);
        textView.setText(dbInfos.getDescription());

        textView = findViewById(R.id.textVersion);
        textView.setText(dbInfos.getVersion());

        if (Collection.getInstance().getNbProperties() > 0) {
            createPropertiesList();
        } else {
            textView = findViewById(R.id.textNoProperties);
            textView.setVisibility(View.VISIBLE);
        }

        displayHelpButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_database_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.save_btn:
                saveDatas();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveDatas() {
        // Get new datas
        CollectionInfos dbInfos = Collection.getInstance().getInfos();
        EditText editText = findViewById(R.id.textName);
        if (editText.getText().length() == 0) {
            editText.setError(getString(R.string.must_not_be_empty));
            return;
        }
        dbInfos.setName(editText.getText().toString());

        editText = findViewById(R.id.textDescription);
        if (editText.getText().length() == 0) {
            editText.setError(getString(R.string.must_not_be_empty));
            return;
        }
        dbInfos.setDescription(editText.getText().toString());

        editText = findViewById(R.id.textVersion);
        if (editText.getText().length() == 0) {
            editText.setError(getString(R.string.must_not_be_empty));
            return;
        }
        dbInfos.setVersion(editText.getText().toString());

        List<Property> properties = Collection.getInstance().getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            for (Property desc : properties) {
                editText = findViewById(desc.getId());
                desc.setDescription(editText.getText().toString());
            }
        }

        // Finally write datas to XML file
        if (xmlFactory.writeXml())
            Toast.makeText(getApplicationContext(), R.string.successfully_saved, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), R.string.problem_during_save, Toast.LENGTH_LONG).show();
    }

    public void addProperty(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new_something);
        dialog.setTitle(getString(R.string.new_property));

        TextView textView = dialog.findViewById(R.id.message);
        textView.setText(R.string.message_new_property);

        Button btnOK = dialog.findViewById(R.id.btn_ok);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText propertyName = dialog.findViewById(R.id.propertyName);
                String name = propertyName.getText().toString();
                if (name.isEmpty()) {
                    propertyName.setError(getString(R.string.must_not_be_empty));
                    return;
                }
                dialog.dismiss();
                createNewProperty(name);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                dialog.cancel();
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void createNewProperty(String name) {
        Property property = new Property();
        property.setName(name);
        property.setId(View.generateViewId());
        property.setDescription(null);
        Collection.getInstance().addProperty(property);

        addProperty(property);

        TextView textView = findViewById(R.id.textNoProperties);
        textView.setVisibility(View.GONE);

        final ScrollView scrollView = findViewById(R.id.detailsScrollview);
        scrollView.post(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void addProperty(Property property) {
        LinearLayout linearLayout = findViewById(R.id.linearlayout);

        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(property.getName());
        textView.setTextColor(getColor(R.color.black));
        textView.setPadding(20, 20, 20, 20);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTypeface(null, Typeface.BOLD);
        linearLayout.addView(textView);

        EditText editText = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP;
        lp.setMargins(10, 10, 10, 10);
        editText.setLayoutParams(lp);
        editText.setHint(R.string.to_be_completed);
        editText.setHintTextColor(getColor(R.color.dark_gray));
        editText.setGravity(Gravity.TOP);
        if (property.getDescription() != null)
            editText.setText(property.getDescription());
        editText.setPadding(editText.getPaddingLeft(), 0, editText.getPaddingRight(), editText.getPaddingBottom());
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        editText.setSingleLine(false);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setId(property.getId());
        linearLayout.addView(editText);
    }

    private void createPropertiesList() {
        List<Property> properties = Collection.getInstance().getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            for (Property property : properties) {
                addProperty(property);
            }
        }
    }
}
