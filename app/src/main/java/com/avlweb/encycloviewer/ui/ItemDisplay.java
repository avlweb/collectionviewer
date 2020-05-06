package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;
import com.avlweb.encycloviewer.model.Property;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class ItemDisplay extends BaseActivity {
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    private int position;
    private boolean detailsOpen = false;
    private DbItem currentItem = null;
    private boolean imageZoomed;
    private EncycloDatabase database = EncycloDatabase.getInstance();
    private int imgIdx = 0;
    private int maxPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_display);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        Intent intent = getIntent();
        this.position = intent.getIntExtra("position", 0);
        this.maxPosition = intent.getIntExtra("maxPosition", 0);

        displayHelpButton();

        // Get preferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
        int fontSize = pref.getInt(Settings.KEY_FONT_SIZE, 0);
        TextView textView = findViewById(R.id.textView3);
        textView.setTextSize(Settings.getFontSizeFromPref(fontSize));
        textView = findViewById(R.id.textViewDetails);
        textView.setTextSize(Settings.getFontSizeFromPref(fontSize));
        textView = findViewById(R.id.textViewDetails2);
        textView.setTextSize(Settings.getFontSizeFromPref(fontSize));

        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        ImageView imageView = findViewById(R.id.imageView1);
        imageView.setOnTouchListener(gestureListener);

        displayElement();
    }

    public void zoomImage(View v) {
        ConstraintLayout mConstrainLayout = findViewById(R.id.myclayout);
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mConstrainLayout.getLayoutParams();
        if (!imageZoomed) {
            lp.matchConstraintPercentHeight = (float) 0.95;
            View tmpView = findViewById(R.id.view2);
            tmpView.setVisibility(View.GONE);
            tmpView = findViewById(R.id.view3);
            tmpView.setVisibility(View.GONE);
            TextView textView = findViewById(R.id.textViewDetails);
            textView.setVisibility(View.GONE);
            textView = findViewById(R.id.textViewDetails2);
            textView.setVisibility(View.GONE);
            ScrollView scrollView = findViewById(R.id.scrollview);
            scrollView.setVisibility(View.GONE);
            ImageButton button = findViewById(R.id.buttonPrevious);
            button.setVisibility(View.GONE);
            button = findViewById(R.id.buttonBottom);
            button.setVisibility(View.GONE);
            imageZoomed = true;
        } else {
            lp.matchConstraintPercentHeight = (float) 0.5;
            View tmpView = findViewById(R.id.view2);
            tmpView.setVisibility(View.VISIBLE);
            tmpView = findViewById(R.id.view3);
            tmpView.setVisibility(View.VISIBLE);
            TextView textView = findViewById(R.id.textViewDetails);
            textView.setVisibility(View.VISIBLE);
            textView = findViewById(R.id.textViewDetails2);
            textView.setVisibility(View.VISIBLE);
            ScrollView scrollView = findViewById(R.id.scrollview);
            scrollView.setVisibility(View.VISIBLE);
            detailsOpen = true;
            openDetails(null);
            imageZoomed = false;
        }
        mConstrainLayout.setLayoutParams(lp);
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                    zoomImage(null);
                } else if ((e1.getX() - e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    displayNextElement();
                } else if ((e2.getX() - e1.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    displayPreviousElement();
                }
            } catch (Exception ignored) {
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // display next image
            showNextImage();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("position", this.position);
            setResult(Activity.RESULT_OK, resultIntent);
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayNextElement() {
        if (this.position < this.maxPosition) {
            this.position++;
            displayElement();
        }
    }

    public void displayPreviousElement() {
        if (this.position > 0) {
            this.position--;
            displayElement();
        }
    }

    private void displayElement() {
        currentItem = database.getItem(this.position);

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

        setTitle(currentItem.getName());

        if ((currentItem.getDescription() != null) && (currentItem.getDescription().length() > 0)) {
            TextView textView = findViewById(R.id.textView3);
            textView.setText(currentItem.getDescription());
        }

        // Format details string
        List<Property> properties = database.getProperties();
        if ((properties != null) && (properties.size() > 0)) {
            int size = properties.size();
            StringBuilder tmp = new StringBuilder();
            for (int idx = 0; idx < size; idx++) {
                String value = currentItem.getProperty(idx);
                if ((value != null) && (value.length() > 0)) {
                    tmp.append(properties.get(idx).getName());
                    tmp.append(" : ");
                    tmp.append(currentItem.getProperty(idx));
                    if (idx < (size - 1))
                        tmp.append("\n");
                }
            }
            TextView textView = findViewById(R.id.textViewDetails2);
            textView.setText(tmp.toString());
        }

        if (imgIdx >= currentItem.getNbImages())
            imgIdx = 0;

        displayImage();
    }

    private void displayImage() {
        TextView textView = findViewById(R.id.textView2);
        if (currentItem.getNbImages() == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.format(Locale.getDefault(), getString(R.string.number_slash_number), imgIdx + 1, currentItem.getNbImages()));
        }

        String imagePath = currentItem.getImagePath(imgIdx);
        if (imagePath == null)
            return;

        String absolutePath = database.getInfos().getPath() + File.separatorChar + imagePath;
        absolutePath = absolutePath.replace("\\", "/");
        File imgFile = new File(absolutePath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView imageView = findViewById(R.id.imageView1);
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

    private void showNextImage() {
        if (imgIdx < (currentItem.getNbImages() - 1))
            imgIdx++;
        else
            imgIdx = 0;

        displayImage();
    }

    public void openDetails(View v) {
        ImageButton buttonBottom = findViewById(R.id.buttonBottom);
        ImageButton buttonPrevious = findViewById(R.id.buttonPrevious);
        TextView textView = findViewById(R.id.textViewDetails2);
        if (detailsOpen) {
            buttonBottom.setVisibility(View.VISIBLE);
            buttonPrevious.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            detailsOpen = false;
        } else {
            buttonBottom.setVisibility(View.GONE);
            buttonPrevious.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            detailsOpen = true;
        }
    }
}
