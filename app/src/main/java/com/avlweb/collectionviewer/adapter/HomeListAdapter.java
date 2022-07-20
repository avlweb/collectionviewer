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

import java.util.ArrayList;

public class HomeListAdapter extends ArrayAdapter<CollectionInfos> {
    private customButtonListener customListener;
    private final Context context;
    private final int scrollbarPosition;

    public interface customButtonListener {
        void onButtonClickListener(View view, CollectionInfos infos);

        void onTextClickListener(CollectionInfos infos);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public HomeListAdapter(Context context, ArrayList<CollectionInfos> infos, int scrollbarPosition) {
        super(context, R.layout.my_main_list, infos);
        this.context = context;
        this.scrollbarPosition = scrollbarPosition;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            if (this.scrollbarPosition == 0)
                convertView = inflater.inflate(R.layout.my_list_right, null);
            else
                convertView = inflater.inflate(R.layout.my_list_left, null);
            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.thetext);
            viewHolder.button = convertView.findViewById(R.id.thebutton);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final CollectionInfos infos = getItem(position);

        // Menu is disabled because sample collection is read only
        if (infos.isSampleCollection()) {
            viewHolder.button.setEnabled(false);
            viewHolder.button.setAlpha(0.4f);
        } else {
            viewHolder.button.setEnabled(true);
            viewHolder.button.setAlpha(1.0f);
        }
        viewHolder.text.setText(infos.getName());
        viewHolder.text.setOnClickListener(v -> {
            if (customListener != null) {
                customListener.onTextClickListener(infos);
            }
        });
        final View finalConvertView = convertView;
        viewHolder.button.setOnClickListener(v -> {
            if (customListener != null) {
                customListener.onButtonClickListener(finalConvertView, infos);
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        TextView text;
        ImageButton button;
    }
}
