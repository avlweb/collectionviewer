package com.avlweb.encycloviewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.avlweb.encycloviewer.R;

import java.util.ArrayList;

public class HomeListAdapter extends ArrayAdapter<String> {
    private customButtonListener customListener;
    private Context context;
    private ArrayList<String> data = new ArrayList<String>();

    public interface customButtonListener {
        public void onButtonClickListener(int position, String value);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public HomeListAdapter(Context context, ArrayList<String> dataItem) {
        super(context, R.layout.my_home_list, dataItem);
        this.data = dataItem;
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.activity_home, null);
            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.thetext);
            viewHolder.button = convertView.findViewById(R.id.thebutton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String temp = getItem(position);
        viewHolder.text.setText(temp);
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customListener != null) {
                    customListener.onButtonClickListener(position, temp);
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
