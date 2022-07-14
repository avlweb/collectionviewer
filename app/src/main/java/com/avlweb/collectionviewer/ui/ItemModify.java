package com.avlweb.collectionviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.exifinterface.media.ExifInterface;

import com.avlweb.collectionviewer.R;
import com.avlweb.collectionviewer.model.CollectionItem;
import com.avlweb.collectionviewer.model.CollectionModel;
import com.avlweb.collectionviewer.model.CollectionProperty;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ItemModify extends BaseActivity {
    private final int ACTIVITY_ADD_IMAGE = 10254841;
    private final DisplayMetrics metrics = new DisplayMetrics();
    private int position;
    private CollectionItem currentItem = null;
    private boolean imageZoomed;
    private final CollectionModel collectionModel = CollectionModel.getInstance();
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
        Log.d("ItemModify", "Metrics : width = " + metrics.widthPixels + ", height = " + metrics.heightPixels);

        Intent intent = getIntent();
        this.position = intent.getIntExtra("position", 0);

        List<CollectionProperty> properties = collectionModel.getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            LinearLayout linearLayout = findViewById(R.id.linearlayout);
            for (CollectionProperty description : properties) {
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
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
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
                (dialog, arg1) -> dialog.cancel());
        alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                (arg0, arg1) -> {
                    currentItem.deleteImage(currentImageIndex);
                    currentImageIndex = 0;
                    displayImage();
                    Toast.makeText(getApplicationContext(), R.string.image_deletion_successful, Toast.LENGTH_SHORT).show();
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
            case (R.id.save_btn):
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

        startActivityForResult(intent, ACTIVITY_ADD_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == ACTIVITY_ADD_IMAGE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the file that the user selected.
            if (resultData != null) {
                Uri uri = resultData.getData();
                if (uri != null) {
                    File collectionPath = new File(collectionModel.getInfos().getPath());
                    Log.d("ItemModify", "collection path = " + collectionPath.getAbsolutePath());
                    String finalPath = collectionPath.getAbsolutePath() + File.separator + "images";
                    Log.d("ItemModify", "images path = " + collectionPath.getAbsolutePath());
                    String uriPath = uri.getPath();
                    File externalPath = Environment.getExternalStorageDirectory();
                    if ((uriPath != null) && (uriPath.length() > 0) && (externalPath != null)) {
                        Log.d("SETTINGS", "default path = " + collectionPath.getAbsolutePath());
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
                        // Copy image to collection images folder
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
        // Get preferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
        // Get flag "Reduce size of images button"
        boolean reduceImagesSizeButton = pref.getBoolean(Settings.KEY_REDUCE_SIZE_OF_IMAGES, false);

        File source = new File(sourcePath);
        Bitmap myBitmap = BitmapFactory.decodeFile(source.getAbsolutePath());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        FileOutputStream fos = null;
        try {
            ExifInterface exif = new ExifInterface(source.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            Bitmap finalBitmap = Bitmap.createBitmap(myBitmap, 0, 0,
                    myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);

            if (reduceImagesSizeButton) {
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            } else {
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            }

            byte[] bitmapped = bos.toByteArray();
            fos = new FileOutputStream(destPath);
            fos.write(bitmapped);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        List<CollectionProperty> properties = collectionModel.getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            for (CollectionProperty description : properties) {
                editText = findViewById(description.getId());
                if ((editText.getText() != null) && (editText.getText().length() > 0)) {
                    currentItem.setProperty(idx, editText.getText().toString());
                } else {
                    currentItem.setProperty(idx, Home.NO_VALUE);
                }
                idx++;
            }
        }
        Toast.makeText(getApplicationContext(), R.string.item_successfully_saved, Toast.LENGTH_SHORT).show();
    }

    private void displayItem() {
        currentItem = null;

        List<CollectionItem> items = collectionModel.getItems();
        if ((items != null) && (items.size() > 0)) {
            for (CollectionItem item : items) {
                if (item.getPositionInSelectedList() == this.position) {
                    currentItem = item;
                    break;
                }
            }
        }

        if (currentItem == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.element_not_found));
            builder.setIcon(R.drawable.ic_launcher);
            builder.setMessage(getString(R.string.list_position) + this.position);
            builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.cancel());
            builder.create().show();
            return;
        }

        if (currentImageIndex >= currentItem.getNbImages()) {
            currentImageIndex = 0;
        }

        // Display name
        EditText editText = findViewById(R.id.textName);
        editText.setText(currentItem.getName());
        // Display description if exists
        if ((currentItem.getDescription() != null) && (currentItem.getDescription().length() > 0)) {
            editText = findViewById(R.id.textDescription);
            editText.setText(currentItem.getDescription());
        }
        // Display properties if exists
        List<CollectionProperty> properties = collectionModel.getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            int idx = 0;
            for (CollectionProperty description : properties) {
                editText = findViewById(description.getId());
                String property = currentItem.getProperty(idx);
                if ((property != null) && (property.length() > 0) && (!property.equals(Home.NO_VALUE))) {
                    editText.setText(property);
                }
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
            ImageButton rubbish = findViewById(R.id.buttonDelete);
            rubbish.setVisibility(View.GONE);
            return;
        } else {
            TextView textView = findViewById(R.id.textView2);
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.format(Locale.getDefault(), getString(R.string.number_slash_number), currentImageIndex + 1, currentItem.getNbImages()));
            textView = findViewById(R.id.textView1);
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            ImageButton rubbish = findViewById(R.id.buttonDelete);
            rubbish.setVisibility(View.VISIBLE);
        }

        String imagePath = currentItem.getImagePath(currentImageIndex);
        if (imagePath == null) {
            return;
        }

        String absolutePath = collectionModel.getInfos().getPath() + File.separatorChar + imagePath;
        absolutePath = absolutePath.replace("\\", "/");
        File imgFile = new File(absolutePath);
        if (imgFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            int scale = (int) (myBitmap.getWidth() / metrics.widthPixels);
            if (scale > 0)
                options.inSampleSize = scale;
            options.inJustDecodeBounds = false;

            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
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
            builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.cancel());
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
