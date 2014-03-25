package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListView;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.constants.TestInformation;
import com.seven.asimov.test.tool.utils.InfoListAdapterUtil;

import java.util.ArrayList;

public class InformationActivity extends Activity implements TestInformation {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infolayout);
        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.mainExpandableListView);
        ArrayList<HeaderInfo> groups = new ArrayList<HeaderInfo>();
        String[] headerArray = getResources().getStringArray(R.array.header);
        for (String header : headerArray) {
            groups.add(new HeaderInfo(header));
        }
        String[] envTests = getResources().getStringArray(R.array.envChildrenList);
        for (int i = 0; i < envTests.length; i++) {
            groups.get(0).addTestInfo(new HeaderInfo.TestInfo(envTests[i], "Steps:\n" + ENV_TESTS.get(i)));
        }

        String[] smokeTests = getResources().getStringArray(R.array.smokeChildrenList);
        for (int i = 0; i < smokeTests.length; i++) {
            groups.get(1).addTestInfo(new HeaderInfo.TestInfo(smokeTests[i], "Steps:\n" + SMOKE_TESTS.get(i)));
        }
        String[] sanityTests = getResources().getStringArray(R.array.sanityChildrenList);
        for (int i = 0; i < sanityTests.length; i++) {
            groups.get(2).addTestInfo(new HeaderInfo.TestInfo(sanityTests[i], "Steps:\n" + SANITY_TESTS.get(i)));
        }
        InfoListAdapterUtil listAdapter = new InfoListAdapterUtil(getApplicationContext(), groups);
        expandableListView.setAdapter(listAdapter);

//        expandableListView.expandGroup(0);
    }
}
