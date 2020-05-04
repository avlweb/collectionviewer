package com.avlweb.encycloviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.NavUtils;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.avlweb.encycloviewer.R;

public class Help extends Activity {

    private static final int MAX_VIEWS = 5;
    private TextView[] dots = new TextView[5];
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setDisplayShowHomeEnabled(false);
        }

        mViewPager = findViewById(R.id.view_pager);
        PagerAdapter adapter = new HelpPagerAdapter();
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new HelpPageChangeListener());

        LinearLayout bottomLayout = findViewById(R.id.bottomLayout);
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(String.format("%d", i + 1));
            dots[i].setPadding(0, 0, 0, 10);
            dots[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            if (i == 0)
                dots[i].setTextColor(getColor(R.color.dark_blue));
            else
                dots[i].setTextColor(getColor(R.color.light_gray));
            bottomLayout.addView(dots[i]);
        }
    }

    private void activateDot(int position) {
        for (int i = 0; i < dots.length; i++) {
            if (position == i) {
                dots[i].setTextColor(getColor(R.color.dark_blue));
            } else {
                dots[i].setTextColor(getColor(R.color.light_gray));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
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
            Log.e("Help", "instantiateItem(" + position + ");");
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View imageViewContainer = inflater.inflate(R.layout.walkthrough_simple_view, null);
            ImageView imageView = imageViewContainer.findViewById(R.id.image_view);

            switch (position) {
                case 0:
                    imageView.setImageResource(R.drawable.help_home);
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.help_mainlist);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.help_database_modify);
                    break;
                case 3:
                    imageView.setImageResource(R.drawable.help_item_display);
                    break;
                case 4:
                    imageView.setImageResource(R.drawable.help_item_modify);
                    break;
            }

            container.addView(imageViewContainer, 0);
            return imageViewContainer;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
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