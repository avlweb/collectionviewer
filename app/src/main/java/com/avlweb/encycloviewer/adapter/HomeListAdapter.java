package com.avlweb.encycloviewer.adapter;

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
    private customButtonListener customListener;
    private Context context;
    private ArrayList<String> data;
    private String databasesRootLocation;

    public interface customButtonListener {
        void onButtonClickListener(View view, int position, String value);

        void onTextClickListener(int position, String value);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public HomeListAdapter(Context context, ArrayList<String> dataItem, String location) {
        super(context, R.layout.my_home_list, dataItem);
        this.data = dataItem;
        this.context = context;
        this.databasesRootLocation = location;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            convertView = inflater.inflate(R.layout.my_home_list, null);
            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.thetext);
            viewHolder.button = convertView.findViewById(R.id.thebutton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String temp = getItem(position);
        if (temp.endsWith("Sample_database.xml")) {
            viewHolder.text.setText("Sample_database.xml");
            viewHolder.button.setVisibility(View.GONE);
        } else if ((this.databasesRootLocation != null) && (temp.startsWith(this.databasesRootLocation)))
            viewHolder.text.setText(temp.substring(this.databasesRootLocation.length()));
        else
            viewHolder.text.setText(temp);
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

    public class ViewHolder {
        TextView text;
        ImageButton button;
    }
}
