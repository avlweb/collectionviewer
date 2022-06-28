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
import com.avlweb.collectionviewer.ui.MainList;

import java.util.ArrayList;

public class HomeListAdapter extends ArrayAdapter<CollectionInfos> {
    private customButtonListener customListener;
    private final Context context;
    private final String databasesRootLocation;
    private final int scrollbarPosition;

    public interface customButtonListener {
        void onButtonClickListener(View view, int position, CollectionInfos value);
        void onTextClickListener(int position, CollectionInfos value);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public HomeListAdapter(Context context, ArrayList<CollectionInfos> infos, String location, int scrollbarPosition) {
        super(context, R.layout.my_main_list, infos);
        this.context = context;
        this.databasesRootLocation = location;
        this.scrollbarPosition = scrollbarPosition;
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
        final CollectionInfos temp = getItem(position);
        if (temp.getName().equals(MainList.SAMPLE_COLLECTION_NAME)) {
            // Menu is disabled because sample database is read only
            viewHolder.button.setEnabled(false);
            viewHolder.text.setText(MainList.SAMPLE_COLLECTION_NAME);
        } else {
            viewHolder.button.setEnabled(true);
            if ((this.databasesRootLocation != null) && (temp.getName().startsWith(this.databasesRootLocation)))
                viewHolder.text.setText(temp.getName().substring(this.databasesRootLocation.length()));
            else
                viewHolder.text.setText(temp.getName());
        }
        viewHolder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customListener != null) {
                    customListener.onTextClickListener(position, temp);
                }
            }
        });
        final View finalConvertView = convertView;
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customListener != null) {
                    customListener.onButtonClickListener(finalConvertView, position, temp);
                }
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        TextView text;
        ImageButton button;
    }
}
