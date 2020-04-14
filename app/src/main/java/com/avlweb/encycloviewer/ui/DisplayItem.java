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

import com.avlweb.encycloviewer.R;
import com.avlweb.encycloviewer.model.DbItem;

import java.io.File;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;

public class DisplayItem extends Activity implements View.OnClickListener {

    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    private int position;
    private boolean detailsOpen = false;
    private DbItem currentElement = null;
    private boolean imageZoomed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_item);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        Intent intent = getIntent();
        this.position = intent.getIntExtra("position", 0);

        // Get preferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Settings.KEY_PREFS, MODE_PRIVATE);
        int fontSize = pref.getInt(Settings.KEY_FONT_SIZE, 0);
        TextView textDescription = findViewById(R.id.textView3);
        textDescription.setTextSize(Settings.getFontSizeFromPref(fontSize));

        // Gesture detection
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
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
            lp.matchConstraintPercentHeight = (float) 0.90;
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
            lp.matchConstraintPercentHeight = (float) 0.45;
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
        if (this.position < MainList.maxPosition) {
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

        for (int i = 0; i < MainList.itemsList.size(); i++) {
            currentElement = MainList.itemsList.get(i);
            if ((currentElement.getListPosition() != -1) &&
                    (currentElement.getListPosition() == this.position))
                break;
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
        }

        MainList.selectedItemPosition = this.position;

        TextView textView1 = findViewById(R.id.textView1);
        textView1.setText(currentElement.getField(0));

        TextView textView2 = findViewById(R.id.textView3);
        textView2.setText(currentElement.getField(1));

        if (MainList.imgIdx >= currentElement.getNbImages())
            MainList.imgIdx = 0;

        TextView textView3 = findViewById(R.id.textView2);
        textView3.setText(String.format(getString(R.string.number_slash_number), MainList.imgIdx + 1, currentElement.getNbImages()));

        TextView textView4 = findViewById(R.id.textViewDetails2);
        textView4.setText(String.format("%s : %s\n%s : %s\n%s : %s",
                MainList.dbInfos.getFieldName(2), currentElement.getField(2), MainList.dbInfos.getFieldName(3),
                currentElement.getField(3), MainList.dbInfos.getFieldName(4), currentElement.getField(4)));

        displayImage();
    }

    private void displayElementDetails() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.details));
        builder.setIcon(R.drawable.ic_launcher);
        builder.setMessage(MainList.dbInfos.getFieldName(2) + " : " + currentElement.getField(2)
                + "\n" + MainList.dbInfos.getFieldName(3) + " : " + currentElement.getField(3)
                + "\n" + MainList.dbInfos.getFieldName(4) + " : " + currentElement.getField(4));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void displayImage() {
        String imgpath = MainList.dbpath + File.separatorChar +
                currentElement.getImagePath(MainList.imgIdx);
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
        if (MainList.imgIdx < (currentElement.getNbImages() - 1))
            MainList.imgIdx++;
        else
            MainList.imgIdx = 0;

        TextView editText3 = findViewById(R.id.textView2);
        editText3.setText(String.format(getString(R.string.number_slash_number), MainList.imgIdx + 1, currentElement.getNbImages()));

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

    @Override
    public void onClick(View v) {
    }
}
