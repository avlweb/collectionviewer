package com.avlweb.encycloviewer.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import com.avlweb.encycloviewer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainListAdapter extends ArrayAdapter<String> implements SectionIndexer {

    private HashMap<String, Integer> mapIndex;
    private String[] sections;

    public MainListAdapter(Context context, List<String> list) {
        super(context, R.layout.my_main_list, list);

        mapIndex = new LinkedHashMap<>();

        int len = list.size();
        for (int x = 0; x < len; x++) {
            String item = list.get(x);
            String ch = item.substring(0, 1).toUpperCase(Locale.FRANCE);

            // HashMap will prevent duplicates
            if (!mapIndex.containsKey(ch)) {
                mapIndex.put(ch, x);
            }
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);

        //Log.d("sectionList", sectionList.toString());
        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);
    }

    public int getPositionForSection(int section) {
        //Log.d("getPositionForSection ", "" + section);
        return mapIndex.get(sections[section]);
    }

    public int getSectionForPosition(int position) {
        //Log.d("getSectionForPosition ", "" + position);
        return 0;
    }

    public Object[] getSections() {
        return sections;
    }
}
