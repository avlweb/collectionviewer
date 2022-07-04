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

import java.util.List;

public class MainListAdapter extends ArrayAdapter<CollectionItem> {
    private final CollectionInfos infos;
    private final int scrollbarPosition;
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

        this.scrollbarPosition = scrollbarPosition;

        this.infos = CollectionModel.getInstance().getInfos();
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
        final CollectionItem temp = getItem(position);

        // Menu is disabled because sample collection is read only
        if (this.infos.isSampleCollection()) {
            viewHolder.button.setEnabled(false);
            viewHolder.button.setAlpha(0.2f);
        } else {
            viewHolder.button.setEnabled(true);
            viewHolder.button.setAlpha(1.0f);
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
}
