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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NavUtils;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DatabaseInfos;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.model.FieldDescription;
import com.avlweb.encycloviewer.util.xmlFactory;

import java.util.List;

public class DatabaseModify extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_database_modify);

        setTitle(getString(R.string.database_modify));

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

        if (EncycloDatabase.getInstance().getNbFields() > 0) {
            createFieldList();
        } else {
            textView = findViewById(R.id.textNoFields);
            textView.setVisibility(View.VISIBLE);
        }
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
        DatabaseInfos dbInfos = EncycloDatabase.getInstance().getInfos();
        TextView textView = findViewById(R.id.textName);
        dbInfos.setName(textView.getText().toString());

        textView = findViewById(R.id.textDescription);
        dbInfos.setDescription(textView.getText().toString());

        textView = findViewById(R.id.textVersion);
        dbInfos.setVersion(textView.getText().toString());

        List<FieldDescription> descs = EncycloDatabase.getInstance().getFieldDescriptions();
        for (FieldDescription desc : descs) {
            EditText editText = findViewById(desc.getId());
            desc.setDescription(editText.getText().toString());
        }

        // Finally write datas to XML file
        if (xmlFactory.writeXml())
            Toast.makeText(getApplicationContext(), R.string.successfully_saved, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), R.string.problem_during_save, Toast.LENGTH_LONG).show();
    }

    public void addField(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialog = inflater.inflate(R.layout.dialog_new_field, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.new_field));
        alertDialog.setCancelable(true);
        final EditText fieldName = dialog.findViewById(R.id.fieldName);
        fieldName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideKeyboard();
                String name = fieldName.getText().toString();
                if (name.length() > 0) {
                    addNewField(name);
                }
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideKeyboard();
                alertDialog.dismiss();
            }
        });

        alertDialog.setView(dialog);
        alertDialog.show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void addNewField(String name) {
        FieldDescription field = new FieldDescription();
        field.setName(name);
        field.setId(View.generateViewId());
        field.setDescription(null);
        EncycloDatabase.getInstance().addFieldDescription(field);

        addField(field);

        TextView textView = findViewById(R.id.textNoFields);
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

    private void addField(FieldDescription field) {
        LinearLayout linearLayout = findViewById(R.id.linearlayout);

        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setText(field.getName());
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
        if (field.getDescription() != null)
            editText.setText(field.getDescription());
        editText.setPadding(editText.getPaddingLeft(), 0, editText.getPaddingRight(), editText.getPaddingBottom());
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        editText.setSingleLine(false);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setId(field.getId());
        linearLayout.addView(editText);
    }

    private void createFieldList() {
        for (FieldDescription field : EncycloDatabase.getInstance().getFieldDescriptions()) {
            addField(field);
        }
    }
}
