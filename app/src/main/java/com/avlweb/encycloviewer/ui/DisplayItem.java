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
import androidx.core.app.NavUtils;

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DbItem;
import com.avlweb.encycloviewer.model.EncycloDatabase;

import java.io.File;

public class DisplayItem extends Activity {
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    private int position;
    private boolean detailsOpen = false;
    private DbItem currentElement = null;
    private boolean imageZoomed;
    private EncycloDatabase database = EncycloDatabase.getInstance();
    private int imgIdx = 0;
    private int maxPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_item);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        Intent intent = getIntent();
        this.position = intent.getIntExtra("position", 0);
        this.maxPosition = intent.getIntExtra("maxPosition", 0);

        // Get preferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
        int fontSize = pref.getInt(Settings.KEY_FONT_SIZE, 0);
        TextView textDescription = findViewById(R.id.textView3);
        textDescription.setTextSize(Settings.getFontSizeFromPref(fontSize));

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

    public void ZoomImage(View v) {
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
            OpenDetails(null);
            imageZoomed = false;
        }
        mConstrainLayout.setLayoutParams(lp);
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                    ZoomImage(null);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.details_btn:
                displayElementDetails();
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
        currentElement = null;

        for (DbItem item : database.getItemsList()) {
            if ((item.getListPosition() != -1) && (item.getListPosition() == this.position)) {
                currentElement = item;
                break;
            }
        }

        if (currentElement == null) {
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

        MainList.selectedItemPosition = this.position;

        setTitle(currentElement.getField(0));

        TextView textView2 = findViewById(R.id.textView3);
        textView2.setText(currentElement.getField(1));

        if (imgIdx >= currentElement.getNbImages())
            imgIdx = 0;

        TextView textView3 = findViewById(R.id.textView2);
        textView3.setText(String.format(getString(R.string.number_slash_number), imgIdx + 1, currentElement.getNbImages()));

        TextView textView4 = findViewById(R.id.textViewDetails2);
        textView4.setText(String.format("%s : %s\n%s : %s\n%s : %s",
                database.getFieldName(2), currentElement.getField(2), database.getFieldName(3),
                currentElement.getField(3), database.getFieldName(4), currentElement.getField(4)));

        displayImage();
    }

    private void displayElementDetails() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.details));
        builder.setIcon(R.drawable.ic_launcher);
        builder.setMessage(database.getFieldName(2) + " : " + currentElement.getField(2)
                + "\n" + database.getFieldName(3) + " : " + currentElement.getField(3)
                + "\n" + database.getFieldName(4) + " : " + currentElement.getField(4));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void displayImage() {
        String imgpath = database.getInfos().getPath() + File.separatorChar + currentElement.getImagePath(imgIdx);
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

    private void showNextImage() {
        if (imgIdx < (currentElement.getNbImages() - 1))
            imgIdx++;
        else
            imgIdx = 0;

        TextView editText3 = findViewById(R.id.textView2);
        editText3.setText(String.format(getString(R.string.number_slash_number), imgIdx + 1, currentElement.getNbImages()));

        displayImage();
    }

    public void OpenDetails(View v) {
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
