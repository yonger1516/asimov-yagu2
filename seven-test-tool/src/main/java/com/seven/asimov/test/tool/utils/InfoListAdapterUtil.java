package com.seven.asimov.test.tool.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.activity.HeaderInfo;

import java.util.ArrayList;

public class InfoListAdapterUtil extends BaseExpandableListAdapter {
    private ArrayList<HeaderInfo> mGroups;
    private Context mContext;

    public InfoListAdapterUtil(Context context, ArrayList<HeaderInfo> groups) {
        mContext = context;
        mGroups = groups;
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroups.get(groupPosition).getProductList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition).getProductList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_heading, null);
        }

        TextView textGroup = (TextView) convertView.findViewById(R.id.heading);
        textGroup.setText(mGroups.get(groupPosition).getName());

        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_row, null);
        }
        TextView textChild = (TextView) convertView.findViewById(R.id.childrenTest);
        textChild.setText(mGroups.get(groupPosition).getProductList().get(childPosition).getTestName());
        TextView infoRow = (TextView) convertView.findViewById(R.id.tvInfoRow);
        infoRow.setText(mGroups.get(groupPosition).getProductList().get(childPosition).getInformation());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}
