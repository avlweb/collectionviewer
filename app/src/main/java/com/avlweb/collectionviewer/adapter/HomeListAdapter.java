package com.avlweb.collectionviewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.avlweb.encycloviewer.R;

import java.util.ArrayList;

public class HomeListAdapter extends ArrayAdapter<String> {
    public static final String SAMPLE_COLLECTION_XML = "Sample_collection.xml";
    private customButtonListener customListener;
    private final Context context;
    private final String databasesRootLocation;
    private final int scrollbarPosition;

    public interface customButtonListener {
        void onButtonClickListener(View view, int position, String value);

        void onTextClickListener(int position, String value);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public HomeListAdapter(Context context, ArrayList<String> dataItem, String location, int scrollbarPosition) {
        super(context, R.layout.my_main_list, dataItem);
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
        final String temp = getItem(position);
        if (temp.endsWith(SAMPLE_COLLECTION_XML)) {
            // Line menu is disabled because sample database is read only
            viewHolder.button.setEnabled(false);
            viewHolder.text.setText(SAMPLE_COLLECTION_XML);
        } else {
            viewHolder.button.setEnabled(true);
            if ((this.databasesRootLocation != null) && (temp.startsWith(this.databasesRootLocation)))
                viewHolder.text.setText(temp.substring(this.databasesRootLocation.length()));
            else
                viewHolder.text.setText(temp);
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
