package com.avlweb.encycloviewer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SearchInDatabase extends Activity {
    private static String fSearch1 = null;
    private static String fSearch2 = null;
    private static String fSearch3 = null;
    private static String fSearch4 = null;
    private static String fSearch5 = null;

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

        if (MainList.dbInfos != null) {
            TextView textView = findViewById(R.id.TextView01);
            textView.setText(MainList.dbInfos.getFieldName(1));
            textView = findViewById(R.id.TextView02);
            textView.setText(MainList.dbInfos.getFieldName(2));
            textView = findViewById(R.id.TextView03);
            textView.setText(MainList.dbInfos.getFieldName(3));
            textView = findViewById(R.id.TextView04);
            textView.setText(MainList.dbInfos.getFieldName(4));
            textView = findViewById(R.id.TextView05);
            textView.setText(MainList.dbInfos.getFieldName(5));
        }

        EditText editText;
        if (fSearch1 != null) {
            editText = findViewById(R.id.EditText01);
            editText.setText(fSearch1);
            editText.requestFocus();
        }
        if (fSearch2 != null) {
            editText = findViewById(R.id.EditText02);
            editText.setText(fSearch2);
        }
        if (fSearch3 != null) {
            editText = findViewById(R.id.EditText03);
            editText.setText(fSearch3);
        }
        if (fSearch4 != null) {
            editText = findViewById(R.id.EditText04);
            editText.setText(fSearch4);
        }
        if (fSearch5 != null) {
            editText = findViewById(R.id.EditText05);
            editText.setText(fSearch5);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
    }

    @SuppressWarnings("deprecation")
    public void searchInDatabase(View view) {
        String[] stringsToSearch1, stringsToSearch2, stringsToSearch3, stringsToSearch4, stringsToSearch5;
        int i, j, nbStringsOk;
        EditText editText;
        int nbElementsFound = 0;
        Element element;

        stringsToSearch1 = null;
        stringsToSearch2 = null;
        stringsToSearch3 = null;
        stringsToSearch4 = null;
        stringsToSearch5 = null;

        editText = findViewById(R.id.EditText01);
        fSearch1 = editText.getText().toString();
        if (fSearch1.length() > 0)
            stringsToSearch1 = fSearch1.split(",");

        editText = findViewById(R.id.EditText02);
        fSearch2 = editText.getText().toString();
        if (fSearch2.length() > 0)
            stringsToSearch2 = fSearch2.split(",");

        editText = findViewById(R.id.EditText03);
        fSearch3 = editText.getText().toString();
        if (fSearch3.length() > 0)
            stringsToSearch3 = fSearch3.split(",");

        editText = findViewById(R.id.EditText04);
        fSearch4 = editText.getText().toString();
        if (fSearch4.length() > 0)
            stringsToSearch4 = fSearch4.split(",");

        editText = findViewById(R.id.EditText05);
        fSearch5 = editText.getText().toString();
        if (fSearch5.length() > 0)
            stringsToSearch5 = fSearch5.split(",");

		/*
		AlertDialog alertDialog = new AlertDialog.Builder( SearchInDatabase.this).create();
		alertDialog.setTitle( "Search");
		alertDialog.setMessage( "Criterias : (" + Arrays.toString(stringsToSearch1)
				+ "), (" + Arrays.toString(stringsToSearch2)
				+ "), (" + Arrays.toString(stringsToSearch3)
				+ "), (" + Arrays.toString(stringsToSearch4)
				+ "), (" + Arrays.toString(stringsToSearch5) + ")");
		alertDialog.setButton( "OK", new DialogInterface.OnClickListener()
		{ 
			public void onClick( DialogInterface dialog, int which) { dialog.cancel(); }
		});
		alertDialog.show();
		*/

        if (MainList.itemsList != null) {
            for (i = 0; i < MainList.itemsList.size(); i++) {
                element = MainList.itemsList.get(i);
                element.setNotSelected();
            }

            for (i = 0; i < MainList.itemsList.size(); i++) {
                element = MainList.itemsList.get(i);
                String element1 = element.getField1().toLowerCase();
                String element2 = element.getField2().toLowerCase();
                String element3 = element.getField3().toLowerCase();
                String element4 = element.getField4().toLowerCase();
                String element5 = element.getField5().toLowerCase();
/*
                if (stringsToSearch1 != null) {
                    if (element.getField1() == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch1.length; j++) {
                        if (element.getField1().contains(stringsToSearch1[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch1.length)
                        continue;
                }

                if (stringsToSearch2 != null) {
                    if (element.getField2() == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch2.length; j++) {
                        if (element.getField2().contains(stringsToSearch2[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch2.length)
                        continue;
                }

                if (stringsToSearch3 != null) {
                    if (element.getField3() == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch3.length; j++) {
                        if (element.getField3().contains(stringsToSearch3[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch3.length)
                        continue;
                }

                if (stringsToSearch4 != null) {
                    if (element.getField4() == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch4.length; j++) {
                        if (element.getField4().contains(stringsToSearch4[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch4.length)
                        continue;
                }

                if (stringsToSearch5 != null) {
                    if (element.getField5() == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch5.length; j++) {
                        if (element.getField5().contains(stringsToSearch5[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch5.length)
                        continue;
                }
*/
                if (stringsToSearch1 != null) {
                    if (element1 == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch1.length; j++) {
                        if (element1.contains(stringsToSearch1[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch1.length)
                        continue;
                }

                if (stringsToSearch2 != null) {
                    if (element2 == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch2.length; j++) {
                        if (element2.contains(stringsToSearch2[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch2.length)
                        continue;
                }

                if (stringsToSearch3 != null) {
                    if (element3 == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch3.length; j++) {
                        if (element3.contains(stringsToSearch3[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch3.length)
                        continue;
                }

                if (stringsToSearch4 != null) {
                    if (element4 == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch4.length; j++) {
                        if (element4.contains(stringsToSearch4[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch4.length)
                        continue;
                }

                if (stringsToSearch5 != null) {
                    if (element5 == null)
                        continue;

                    nbStringsOk = 0;
                    for (j = 0; j < stringsToSearch5.length; j++) {
                        if (element5.contains(stringsToSearch5[j]))
                            nbStringsOk++;
                    }
                    if (nbStringsOk != stringsToSearch5.length)
                        continue;
                }

                element.setSelected();
                nbElementsFound++;
            }

            if (nbElementsFound == 0) {
                AlertDialog alertDialog = new AlertDialog.Builder(SearchInDatabase.this).create();
                alertDialog.setTitle(R.string.search);
                alertDialog.setMessage(getString(R.string.no_element_found));
                alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();

                for (i = 0; i < MainList.itemsList.size(); i++) {
                    element = MainList.itemsList.get(i);
                    element.setSelected();
                }
            } else
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.found_elements), nbElementsFound), Toast.LENGTH_SHORT).show();

            MainList.selectedItemPosition = 0;
        }
    }

    public void clearSearch(View view) {
        Element element;

        for (int i = 0; i < MainList.itemsList.size(); i++) {
            element = MainList.itemsList.get(i);
            element.setSelected();
        }

        fSearch1 = null;
        fSearch2 = null;
        fSearch3 = null;
        fSearch4 = null;
        fSearch5 = null;

        EditText editText = findViewById(R.id.EditText01);
        editText.setText("");
        editText = findViewById(R.id.EditText02);
        editText.setText("");
        editText = findViewById(R.id.EditText03);
        editText.setText("");
        editText = findViewById(R.id.EditText04);
        editText.setText("");
        editText = findViewById(R.id.EditText05);
        editText.setText("");

        MainList.selectedItemPosition = 0;
        Toast.makeText(getApplicationContext(), R.string.clear_done, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
