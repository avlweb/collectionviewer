package com.avlweb.collectionviewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.avlweb.collectionviewer.R;
import com.avlweb.collectionviewer.model.CollectionInfos;
import com.avlweb.collectionviewer.model.CollectionItem;
import com.avlweb.collectionviewer.model.CollectionModel;
import com.avlweb.collectionviewer.ui.MainList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

//public class MainListAdapter extends ArrayAdapter<Item> implements SectionIndexer {
public class MainListAdapter extends ArrayAdapter<CollectionItem> {
    private final CollectionInfos infos;
    private final int scrollbarPosition;
    private final HashMap<String, Integer> mapIndex;
    private final String[] sections;
    private customButtonListener customListener;
    private final Context context;

    public interface customButtonListener {
        void onButtonClickListener(View view, int position);

        void onTextClickListener(int position);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public MainListAdapter(Context context, List<CollectionItem> list, int scrollbarPosition) {
        super(context, R.layout.my_main_list, list);
        this.context = context;

        mapIndex = new LinkedHashMap<>();

        int len = list.size();
        for (int x = 0; x < len; x++) {
            String item = getItem(x).getName();
            String ch = item.substring(0, 1).toUpperCase(Locale.FRANCE);

            // HashMap will prevent duplicates
            if (!mapIndex.containsKey(ch)) {
                mapIndex.put(ch, x);
            }
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);

        this.scrollbarPosition = scrollbarPosition;

        this.infos = CollectionModel.getInstance().getInfos();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            if (this.scrollbarPosition == 0)
                convertView = inflater.inflate(R.layout.my_home_list_left, null);
            else
                convertView = inflater.inflate(R.layout.my_home_list_right, null);
            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.thetext);
            viewHolder.button = convertView.findViewById(R.id.thebutton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final CollectionItem temp = getItem(position);

        // Menu is disabled because sample collection is read only
        if ((this.infos.getName().equals(MainList.SAMPLE_COLLECTION_FR_NAME))
                || (this.infos.getName().equals(MainList.SAMPLE_COLLECTION_EN_NAME))) {
            viewHolder.button.setEnabled(false);
            viewHolder.button.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.button.setEnabled(true);
            viewHolder.button.setVisibility(View.VISIBLE);
        }
        viewHolder.text.setText(temp.getName());
        viewHolder.text.setOnClickListener(v -> {
            if (customListener != null) {
                customListener.onTextClickListener(position);
            }
        });
        final View finalConvertView = convertView;
        viewHolder.button.setOnClickListener(v -> {
            if (customListener != null) {
                customListener.onButtonClickListener(finalConvertView, position);
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        TextView text;
        ImageButton button;
    }

    public int getPositionForSection(int section) {
        return mapIndex.get(sections[section]);
    }

    public int getSectionForPosition(int position) {
        return 0;
    }

    public Object[] getSections() {
        return sections;
    }
}
