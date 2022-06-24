package com.avlweb.collectionviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.avlweb.encycloviewer.R;

import java.util.Locale;

public class Help extends Activity {
    private static final int MAX_VIEWS = 5;
    private TextView[] dots = new TextView[5];
    public static final int HELP_HOME = 0;
    public static final int HELP_MAINLIST = 1;
    public static final int HELP_DATABASE_MODIFY = 2;
    public static final int HELP_ITEM_DISPLAY = 3;
    public static final int HELP_ITEM_MODIFY = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        Intent intent = getIntent();
        int origin = intent.getIntExtra("origin", HELP_HOME);

        ViewPager mViewPager = findViewById(R.id.view_pager);
        PagerAdapter adapter = new HelpPagerAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new HelpPageChangeListener());

        LinearLayout bottomLayout = findViewById(R.id.bottomLayout);
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(String.format(Locale.getDefault(), "%d", i + 1));
            dots[i].setPadding(0, 0, 0, 10);
            dots[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            if (i == 0)
                dots[i].setTextColor(getColor(R.color.dark_blue));
            else
                dots[i].setTextColor(getColor(R.color.middle_gray));
            bottomLayout.addView(dots[i]);
        }

        mViewPager.setCurrentItem(origin, true);
    }

    private void activateDot(int position) {
        for (int i = 0; i < dots.length; i++) {
            if (position == i) {
                dots[i].setTextColor(getColor(R.color.dark_blue));
            } else {
                dots[i].setTextColor(getColor(R.color.middle_gray));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class HelpPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return MAX_VIEWS;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View) object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View imageViewContainer = inflater.inflate(R.layout.walkthrough_simple_view, null);
            ImageView imageView = imageViewContainer.findViewById(R.id.image_view);

            // Get current language
            String currentLang = Locale.getDefault().getLanguage();
            // Set image according to position and language
            if ("fr".equals(currentLang)) {
                switch (position) {
                    case HELP_HOME:
                        imageView.setImageResource(R.drawable.help_fr_home);
                        break;
                    case HELP_MAINLIST:
                        imageView.setImageResource(R.drawable.help_fr_mainlist);
                        break;
                    case HELP_DATABASE_MODIFY:
                        imageView.setImageResource(R.drawable.help_fr_database_modify);
                        break;
                    case HELP_ITEM_DISPLAY:
                        imageView.setImageResource(R.drawable.help_fr_item_display);
                        break;
                    case HELP_ITEM_MODIFY:
                        imageView.setImageResource(R.drawable.help_fr_item_modify);
                        break;
                }
            } else {
                switch (position) {
                    case HELP_HOME:
                        imageView.setImageResource(R.drawable.help_en_home);
                        break;
                    case HELP_MAINLIST:
                        imageView.setImageResource(R.drawable.help_en_mainlist);
                        break;
                    case HELP_DATABASE_MODIFY:
                        imageView.setImageResource(R.drawable.help_en_database_modify);
                        break;
                    case HELP_ITEM_DISPLAY:
                        imageView.setImageResource(R.drawable.help_en_item_display);
                        break;
                    case HELP_ITEM_MODIFY:
                        imageView.setImageResource(R.drawable.help_en_item_modify);
                        break;
                }
            }

            container.addView(imageViewContainer, 0);
            return imageViewContainer;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public class HelpPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {
            activateDot(position);
        }
    }
}
