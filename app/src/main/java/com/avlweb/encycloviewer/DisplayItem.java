package com.avlweb.encycloviewer;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import androidx.core.app.NavUtils;

public class DisplayItem extends Activity implements View.OnClickListener {

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;

    private int position;
    private DbItem currentElement = null;

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

        displayElement();
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    displayNextElement();
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_display_item, menu);
        return true;
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
/*
            case R.id.next_btn:
                displayNextElement();
                return true;

            case R.id.previous_btn:
                displayPreviousElement();
                return true;
*/
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

        TextView editText1 = findViewById(R.id.textView1);
        editText1.setText(currentElement.getField(1));

        TextView editText2 = findViewById(R.id.textView3);
        editText2.setText(currentElement.getField(2));

        if (MainList.imgIdx > currentElement.getLastImageIndex())
            MainList.imgIdx = 1;

        TextView editText3 = findViewById(R.id.textView2);
        editText3.setText(String.format(getString(R.string.number_slash_number), MainList.imgIdx, currentElement.getLastImageIndex()));

        displayImage();
    }

    private void displayElementDetails() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.details));
        builder.setIcon(R.drawable.ic_launcher);
        builder.setMessage(MainList.dbInfos.getFieldName(3) + " : " + currentElement.getField(3)
                + "\n" + MainList.dbInfos.getFieldName(4) + " : " + currentElement.getField(4)
                + "\n" + MainList.dbInfos.getFieldName(5) + " : " + currentElement.getField(5));
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
            imageView.setOnTouchListener(gestureListener);
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
        if (MainList.imgIdx < currentElement.getLastImageIndex())
            MainList.imgIdx++;
        else
            MainList.imgIdx = 1;

        TextView editText3 = findViewById(R.id.textView2);
        editText3.setText(String.format(getString(R.string.number_slash_number), MainList.imgIdx, currentElement.getLastImageIndex()));

        displayImage();
    }

    @Override
    public void onClick(View v) {
    }
}
