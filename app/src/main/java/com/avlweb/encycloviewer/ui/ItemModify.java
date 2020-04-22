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
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.model.FieldDescription;

import java.io.File;

public class ItemModify extends Activity {
    private DisplayMetrics metrics = new DisplayMetrics();
    private int position;
    private DbItem currentItem = null;
    private boolean imageZoomed;
    private EncycloDatabase database = EncycloDatabase.getInstance();
    private int imgIdx = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_modify);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        Intent intent = getIntent();
        this.position = intent.getIntExtra("position", 0);

        EncycloDatabase database = EncycloDatabase.getInstance();
        if (database.getFieldDescriptions() != null) {
            LinearLayout linearLayout = findViewById(R.id.linearlayout);
            for (FieldDescription field : database.getFieldDescriptions()) {
                TextView textView = new TextView(this);
                textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
                editText.setPadding(editText.getPaddingLeft(), 0, editText.getPaddingRight(), editText.getPaddingBottom());
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                editText.setSingleLine(false);
                editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
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
        imageZoomed = !imageZoomed;
        displayImage();
    }

    public void addImage(View view) {
        Toast.makeText(getApplicationContext(), "Add image", Toast.LENGTH_SHORT).show();
    }

    public void deleteImage(View view) {
        Toast.makeText(getApplicationContext(), "Delete image", Toast.LENGTH_SHORT).show();
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
        // Save name
        EditText editText = findViewById(R.id.textName);
        currentItem.setName(editText.getText().toString());
        // save fields
        for (FieldDescription field : database.getFieldDescriptions()) {
            editText = findViewById(field.getId());
            if ((editText.getText() != null) && (editText.getText().length() > 0)) {
                currentItem.setField(idx, editText.getText().toString());
            }
            idx++;
        }
        Toast.makeText(getApplicationContext(), R.string.item_successfully_saved, Toast.LENGTH_SHORT).show();
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

        EditText editText = findViewById(R.id.textName);
        editText.setText(currentItem.getName());

        EncycloDatabase database = EncycloDatabase.getInstance();
        if (database.getFieldDescriptions() != null) {
            int idx = 0;
            for (FieldDescription field : database.getFieldDescriptions()) {
                editText = findViewById(field.getId());
                editText.setText(currentItem.getField(idx));
                idx++;
            }
        }

        displayImage();
    }

    private void displayImage() {
        ImageView imageView = findViewById(R.id.imageView1);
        TextView textView = findViewById(R.id.textView2);

        if (currentItem.getNbImages() == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.format(getString(R.string.number_slash_number), imgIdx + 1, currentItem.getNbImages()));
        }

        String imagePath = currentItem.getImagePath(imgIdx);
        if (imagePath == null) {
            textView = findViewById(R.id.textView1);
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            return;
        }

        String absolutePath = database.getInfos().getPath() + File.separatorChar + imagePath;
        absolutePath = absolutePath.replace("\\", "/");
        File imgFile = new File(absolutePath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            LinearLayout.LayoutParams llp;
            if (imageZoomed) {
                llp = new LinearLayout.LayoutParams(metrics.widthPixels - 20, (int) (metrics.heightPixels * 0.6));
            } else {
                llp = new LinearLayout.LayoutParams(metrics.widthPixels - 20, (int) (metrics.heightPixels * 0.3));
            }
            llp.setMargins(10, 10, 10, 10);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(llp));
            imageView.setImageBitmap(myBitmap);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_launcher);
            builder.setTitle(getString(R.string.image_not_found));
            builder.setMessage(getString(R.string.path) + absolutePath);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
        }
    }

    public void showNextImage(View view) {
        if (imgIdx < (currentItem.getNbImages() - 1))
            imgIdx++;
        else
            imgIdx = 0;

        displayImage();
    }
}
