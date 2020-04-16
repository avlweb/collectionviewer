package com.avlweb.encycloviewer.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.avlweb.encycloviewer.R;

import java.util.List;

public class HomeListAdapter extends ArrayAdapter<String> {

    private List<String> data;

    public HomeListAdapter(Context context, List<String> list) {
        super(context, R.layout.my_main_list, list);
    }

    public void updateData(List list) {
        this.data = list;
        notifyDataSetChanged();
    }
}
