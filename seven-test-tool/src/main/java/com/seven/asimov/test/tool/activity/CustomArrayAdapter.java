package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import com.seven.asimov.test.tool.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomArrayAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> names;
    private final Map<String, String> results;
    private Boolean global;

    public CustomArrayAdapter(Activity context, ArrayList<String> names, Map<String, String> results) {
        this(context, names, results, null);
    }

    public CustomArrayAdapter(Activity context, ArrayList<String> names, Map<String, String> results, Boolean global/*, ArrayList<String> checkedTest*/) {
        super(context, R.layout.listitem_a, names);
        this.context = context;
        this.names = names;
        this.results = results;
        this.global = global;
        globalMove(names);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listitem_a, parent, false);
        CheckedTextView textView = (CheckedTextView) rowView.findViewById(R.id.label);
        textView.setText(names.get(position).substring(9));

        int currentDrawable;
        if (results.get(names.get(position)).contains("PASSED")) {
            currentDrawable = R.drawable.circle_green;
        } else if (results.get(names.get(position)).contains("FAILED")) {
            currentDrawable = R.drawable.circle_red;
        } else if (results.get(names.get(position)).contains("RUNNING")) {
            currentDrawable = R.drawable.circle_yellow;
        } else if (results.get(names.get(position)).contains("SUSPENDED")) {
            currentDrawable = R.drawable.circle_blue;
        } else if (results.get(names.get(position)).contains("IGNORED")) {
            currentDrawable = R.drawable.circle_turquoise;
        } else {
            currentDrawable = R.drawable.circle_grey;
        }

        if (global != null) {
            if (global) {
                textView.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
            } else {
                textView.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
            }
        } else {
            if (AutomationTestsTab.checkedTest.contains(textView.getText().toString())) {
                textView.setCheckMarkDrawable(android.R.drawable.checkbox_on_background);
            } else {
                textView.setCheckMarkDrawable(android.R.drawable.checkbox_off_background);
            }

        }

        Drawable drawable = context.getResources().getDrawable(currentDrawable);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 0.25), (int) (drawable.getIntrinsicHeight() * 0.25));
        ScaleDrawable sd = new ScaleDrawable(drawable, 0, 0.25f, 0.25f);
        textView.setCompoundDrawables(sd.getDrawable(), null, null, null);

        return rowView;
    }

    private void globalMove(List<String> obj) {
        if (global != null) {
            AutomationTestsTab.checkedTest.clear();

            if (global) {
                for (String s : obj) {
                    AutomationTestsTab.checkedTest.add(s.substring(9));
                }
            }
        }
    }
}
