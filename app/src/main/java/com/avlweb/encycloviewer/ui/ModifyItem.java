package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.model.FieldDescription;
import com.avlweb.encycloviewer.util.xmlFactory;

import java.io.File;

public class ModifyItem extends Activity {
    private int position;
    private DbItem currentItem = null;
    private boolean imageZoomed;
    private EncycloDatabase database = EncycloDatabase.getInstance();
    private int imgIdx = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modify_item);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        Intent intent = getIntent();
        this.position = intent.getIntExtra("position", 0);

        EncycloDatabase database = EncycloDatabase.getInstance();
        if (database.getFieldDescriptions() != null) {
            LinearLayout linearLayout = findViewById(R.id.linearlayout);
            for (FieldDescription field : database.getFieldDescriptions()) {
                TextView textView = new TextView(this);
                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                textView.setText(field.getName());
                textView.setTextColor(getColor(R.color.black));
                textView.setPadding(20, 20, 20, 20);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setTypeface(null, Typeface.BOLD);
                linearLayout.addView(textView);

                EditText editText = new EditText(this);
                editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                editText.setHint(String.format(getString(R.string.words_to_search), field.getName()));
                editText.setHintTextColor(getColor(R.color.dark_gray));
                editText.setGravity(Gravity.TOP);
                editText.setPadding(20, 0, 20, 10);
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                editText.setMinHeight(48);
                editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                editText.setSingleLine();
                editText.setId(field.getId());
                linearLayout.addView(editText);
            }
        }

        displayItem();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_modify_item, menu);
        return true;
    }

    public void zoomImage(View v) {
        ImageView imageView = findViewById(R.id.imageView1);
        if (!imageZoomed) {
            imageView.setMinimumHeight(400);
            imageZoomed = true;
        } else {
            imageView.setMinimumHeight(200);
            imageZoomed = false;
        }
    }

    public void addImage(View view) {
    }

    public void deleteImage(View view) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent resultIntent = new Intent();
                resultIntent.putExtra("position", this.position);
                setResult(Activity.RESULT_OK, resultIntent);
                this.finish();
                return true;
            case R.id.save_btn:
                saveItem();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveItem() {
        int idx = 0;
        for (FieldDescription field : database.getFieldDescriptions()) {
            EditText editText = findViewById(field.getId());
            if ((editText.getText() != null) && (editText.getText().length() > 0)) {
                currentItem.setField(idx, editText.getText().toString());
            }
            idx++;
        }
        if (xmlFactory.writeXml())
            Toast.makeText(getApplicationContext(), R.string.item_successfully_saved, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), R.string.item_error_save, Toast.LENGTH_LONG).show();
    }

    private void displayItem() {
        currentItem = null;

        for (DbItem item : database.getItemsList()) {
            if ((item.getListPosition() != -1) && (item.getListPosition() == this.position)) {
                currentItem = item;
                break;
            }
        }

        if (currentItem == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.element_not_found));
            builder.setIcon(R.drawable.ic_launcher);
            builder.setMessage(getString(R.string.list_position) + this.position);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
            return;
        }

        if (imgIdx >= currentItem.getNbImages())
            imgIdx = 0;

        TextView textView3 = findViewById(R.id.textView2);
        textView3.setText(String.format(getString(R.string.number_slash_number), imgIdx + 1, currentItem.getNbImages()));

        EncycloDatabase database = EncycloDatabase.getInstance();
        if (database.getFieldDescriptions() != null) {
            int idx = 0;
            for (FieldDescription field : database.getFieldDescriptions()) {
                EditText editText = findViewById(field.getId());
                editText.setText(currentItem.getField(idx));
                idx++;
            }
        }

        displayImage();
    }

    private void displayImage() {
        String imgpath = database.getInfos().getPath() + File.separatorChar + currentItem.getImagePath(imgIdx);
        String newPath = imgpath.replace("\\", "/");

        File imgFile = new File(newPath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView imageView = findViewById(R.id.imageView1);
            imageView.setImageBitmap(myBitmap);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_launcher);
            builder.setTitle(getString(R.string.image_not_found));
            builder.setMessage(getString(R.string.path) + newPath);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
        }
    }

    private void showNextImage(View view) {
        if (imgIdx < (currentItem.getNbImages() - 1))
            imgIdx++;
        else
            imgIdx = 0;

        TextView editText3 = findViewById(R.id.textView2);
        editText3.setText(String.format(getString(R.string.number_slash_number), imgIdx + 1, currentItem.getNbImages()));

        displayImage();
    }
}
