package com.seven.asimov.test.tool.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.core.Starter;

/**
 * RootTab TabActivity.
 */

public class RootTab extends TabActivity {

//    private static final String LOG = "RootTab";

    // Controls
    private TabHost mTabHost;

    private static int sCurrentTab = 0;
    private static Tabs sLastPausedTab;

    public static Tabs getCurrentTab() {
        return Tabs.valueOf(sCurrentTab);
    }

    public static void setLastPausedTab(Tabs tab) {
        sLastPausedTab = tab;
    }

    public static Tabs getLastPausedTab() {
        return sLastPausedTab;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main);

        Starter.init(this);

        mTabHost = getTabHost(); // The activity TabHost
        mTabHost.setOnTabChangedListener(mTabChangeListener);
        displayTabs();

    }

    private void displayTabs() {

        TabSpec tabSpec = mTabHost.newTabSpec(Tabs.MAIN_TAB.name());
        TextView tview = new TextView(this);
        tview.setText("Main");
        tview.setTextColor(getResources().getColor(R.color.dark_grey));
        tview.setTextSize(14);
        tview.setPadding(5, 5, 5, 5);
        tview.setGravity(Gravity.CENTER);
        tabSpec.setIndicator(tview);
        // Create an Intent to launch an Activity for the tab (to be reused)
        Intent intent = new Intent(this, MainTab.class);
        // Initialize a TabSpec for each tab and add it to the TabHost
        tabSpec.setContent(intent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(Tabs.ADMIN_TAB.name());
        tview = new TextView(this);
        tview.setText("Admin");
        tview.setTextColor(getResources().getColor(R.color.dark_grey));
        tview.setTextSize(14);
        tview.setPadding(5, 5, 5, 5);
        tview.setGravity(Gravity.CENTER);
        tabSpec.setIndicator(tview);
        intent = new Intent(this, AdminTab.class);
        tabSpec.setContent(intent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(Tabs.TESTS_TAB.name());
        tview = new TextView(this);
        tview.setText("Tests");
        tview.setTextColor(getResources().getColor(R.color.dark_grey));
        tview.setTextSize(14);
        tview.setPadding(5, 5, 5, 5);
        tview.setGravity(Gravity.CENTER);
        tabSpec.setIndicator(tview);
        intent = new Intent(this, TestsTab.class);
        tabSpec.setContent(intent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(Tabs.AUTOMATION_TESTS_TAB.name());
        tview = new TextView(this);
        tview.setText("Automated Tests");
        tview.setTextColor(getResources().getColor(R.color.dark_grey));
        tview.setTextSize(14);
        tview.setPadding(5, 5, 5, 5);
        tview.setGravity(Gravity.CENTER);
        tabSpec.setIndicator(tview);
        intent = new Intent(this, AutomationTestsTab.class);
        tabSpec.setContent(intent);
        mTabHost.addTab(tabSpec);

        // Restore current tab
        mTabHost.setCurrentTab(sCurrentTab);
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Log.v(LOG, "onPause()");
        // http://developer.android.com/reference/android/app/Activity.html
        // Note that it is important to save persistent data in onPause() instead of onSaveInstanceState(Bundle) because
        // the latter is not part of the lifecycle callbacks, so will not be called in every situation as described in
        // its documentation
        // Store UI data
        sCurrentTab = mTabHost.getCurrentTab();
    }

    @Override
    public void onResume() {
        super.onResume();
        //initLogParser();
    }

    private OnTabChangeListener mTabChangeListener = new OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            // Log.v(LOG, "onTabChanged()");

            if (sLastPausedTab != null) {
                if (sLastPausedTab.name().equals(tabId)) {
                    sLastPausedTab = null;
                }
            }

            if ((sLastPausedTab == null && mTabHost.getWindowVisibility() == View.GONE)
                    || (mTabHost.getWindowVisibility() == View.VISIBLE)) {

                sCurrentTab = mTabHost.getCurrentTab();

                for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
                    TextView tv = ((TextView) mTabHost.getTabWidget().getChildAt(i));
                    tv.setTextColor(getResources().getColor(R.color.dark_grey));
                }
                TextView tv = ((TextView) mTabHost.getTabWidget().getChildAt(sCurrentTab));
                tv.setTextColor(getResources().getColor(R.color.white));
            }
        }
    };
}
