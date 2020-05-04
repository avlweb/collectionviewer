package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.avlweb.encycloviewer.model.Property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Locale;

public class ItemModify extends BaseActivity {
    private DisplayMetrics metrics = new DisplayMetrics();
    private int position;
    private DbItem currentItem = null;
    private boolean imageZoomed;
    private EncycloDatabase database = EncycloDatabase.getInstance();
    private int currentImageIndex = 0;

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

        List<Property> properties = database.getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            LinearLayout linearLayout = findViewById(R.id.linearlayout);
            for (Property description : properties) {
                TextView textView = new TextView(this);
                textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                textView.setText(description.getName());
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
                editText.setId(description.getId());
                linearLayout.addView(editText);
            }
        }

        displayHelpButton();

        displayItem();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_item_modify, menu);
        return true;
    }

    public void zoomImage(View v) {
        imageZoomed = !imageZoomed;
        displayImage();
    }

    public void deleteImage(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.warning);
        alertDialogBuilder.setIcon(R.drawable.ic_warning);
        alertDialogBuilder.setMessage(R.string.warning_image_deletion);
        alertDialogBuilder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        currentItem.deleteImage(currentImageIndex);
                        currentImageIndex = 0;
                        displayImage();
                        Toast.makeText(getApplicationContext(), R.string.image_deletion_successful, Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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

    public void addImage(View view) {
        // Choose a file using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        startActivityForResult(intent, 10254841);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == 10254841 && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the file that the user selected.
            if (resultData != null) {
                Uri uri = resultData.getData();
                if (uri != null) {
                    File databasePath = new File(database.getInfos().getPath());
                    Log.d("ItemModify", "database path = " + databasePath.getAbsolutePath());
                    String finalPath = databasePath.getAbsolutePath() + File.separator + "images";
                    Log.d("ItemModify", "images path = " + databasePath.getAbsolutePath());
                    String uriPath = uri.getPath();
                    File externalPath = Environment.getExternalStorageDirectory();
                    if ((uriPath != null) && (uriPath.length() > 0) && (externalPath != null)) {
                        Log.d("SETTINGS", "default path = " + databasePath.getAbsolutePath());
                        Log.d("SETTINGS", "uri path = " + uriPath);
                        String[] tmp = uriPath.split("/");
                        String imageName = tmp[tmp.length - 1];
                        Log.d("SETTINGS", "file name = " + imageName);
                        finalPath += File.separator + tmp[tmp.length - 1];
                        String sourcePath = externalPath.getAbsolutePath() + File.separator;
                        if (uriPath.startsWith("/document/home:")) {
                            String[] paths = uriPath.split(":");
                            if (paths.length == 2)
                                sourcePath += "Documents" + File.separator + paths[1];
                            else
                                sourcePath += "Documents";
                        } else if (uriPath.startsWith("/document/primary:")) {
                            String[] paths = uriPath.split(":");
                            if (paths.length == 2)
                                sourcePath += paths[1];
                        }
                        Log.d("ItemModify", "source path = " + sourcePath);
                        Log.d("ItemModify", "final path = " + finalPath);
                        // Copy image to database images folder
                        copyImage(sourcePath, finalPath);
                        // Save path of image into item
                        currentItem.addImagePath("images" + File.separator + imageName);
                        currentImageIndex = currentItem.getNbImages() - 1;
                        displayImage();
                    }
                }
            }
        }
    }

    private void copyImage(String sourcePath, String destPath) {
        try {
            File source = new File(sourcePath);
            File dest = new File(destPath);
            FileChannel src = new FileInputStream(source).getChannel();
            FileChannel dst = new FileOutputStream(dest).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveItem() {
        int idx = 0;
        // Save name
        EditText editText = findViewById(R.id.textName);
        if (editText.getText().length() == 0) {
            editText.setError(getString(R.string.must_not_be_empty));
            return;
        }
        currentItem.setName(editText.getText().toString());
        // Save description
        editText = findViewById(R.id.textDescription);
        currentItem.setDescription(editText.getText().toString());
        // Save properties
        List<Property> properties = database.getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            for (Property description : properties) {
                editText = findViewById(description.getId());
                if ((editText.getText() != null) && (editText.getText().length() > 0)) {
                    currentItem.setProperty(idx, editText.getText().toString());
                }
                idx++;
            }
        }
        Toast.makeText(getApplicationContext(), R.string.item_successfully_saved, Toast.LENGTH_SHORT).show();
    }

    private void displayItem() {
        currentItem = null;

        for (DbItem item : database.getItems()) {
            if (item.getPositionInSelectedList() == this.position) {
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

        if (currentImageIndex >= currentItem.getNbImages())
            currentImageIndex = 0;

        // Display name
        EditText editText = findViewById(R.id.textName);
        editText.setText(currentItem.getName());
        // Display description if exists
        if ((currentItem.getDescription() != null) && (currentItem.getDescription().length() > 0)) {
            editText = findViewById(R.id.textDescription);
            editText.setText(currentItem.getDescription());
        }
        // Display properties if exists
        List<Property> properties = database.getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            int idx = 0;
            for (Property description : properties) {
                editText = findViewById(description.getId());
                editText.setText(currentItem.getProperty(idx));
                idx++;
            }
        }

        displayImage();
    }

    private void displayImage() {

        ImageView imageView = findViewById(R.id.imageView1);
        if (currentItem.getNbImages() == 0) {
            TextView textView = findViewById(R.id.textView2);
            textView.setVisibility(View.GONE);
            textView = findViewById(R.id.textView1);
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            return;
        } else {
            TextView textView = findViewById(R.id.textView2);
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.format(Locale.getDefault(), getString(R.string.number_slash_number), currentImageIndex + 1, currentItem.getNbImages()));
            textView = findViewById(R.id.textView1);
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }

        String imagePath = currentItem.getImagePath(currentImageIndex);
        if (imagePath == null)
            return;

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
        if (currentImageIndex < (currentItem.getNbImages() - 1))
            currentImageIndex++;
        else
            currentImageIndex = 0;

        displayImage();
    }
}
