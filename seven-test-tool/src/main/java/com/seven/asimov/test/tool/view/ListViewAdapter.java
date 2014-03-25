package com.seven.asimov.test.tool.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.seven.asimov.test.tool.R;

import java.util.List;

public class ListViewAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public ListViewAdapter(Context context, List<String> values) {
        super(context, R.layout.row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row, parent, false);
        TextView rowText = (TextView) rowView.findViewById(R.id.rowtext);
        String s = values.get(position);
        rowText.setText(s);
        int color;
        if (s.endsWith(".target") || s.endsWith(".properties") || s.endsWith(".apk")) {
            color = Color.GREEN;
        } else color = Color.WHITE;
        rowText.setTextColor(color);

        return rowView;
    }
}
