package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.core.TestSuiteMap;
import com.seven.asimov.test.tool.serialization.TestSuite;
import com.seven.asimov.test.tool.utils.Z7FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * SdTestsActivity Activity.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class SdTestsActivity extends Activity {

    private static final String LOG = "SdTestsActivity";

    // http://developer.android.com/resources/articles/listview-backgrounds.html
    private AlertDialog mAlertDialogDeleteTest;
    private ListView mListViewTests;

    private static int sFirstVisibleItem = 0;
    private static int sTop = 0;

    private static TestSuiteMap sTestSuiteMap = new TestSuiteMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests);
        mListViewTests = (ListView) findViewById(R.id.lvTests);
        mListViewTests.setOnItemClickListener(mOnItemClickListener);
        mListViewTests.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
    }

    private void refreshTestSuites() {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_TESTSUITE);
            if (!dir.exists()) {
                Log.d("SD storage:", sdCard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_TESTSUITE
                        + " folder does not exist!");
                if (dir.mkdirs()) {
                    Log.d("SD storage:", sdCard.getAbsolutePath() + Z7FileUtils.EXTERNAL_DIR_TESTSUITE + " created!");
                }
            }

            // This filter returns only xml files
            FileFilter fileFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.getName().endsWith(Z7FileUtils.TESTSUITE_FILE_EXTENSION);
                }
            };

            File[] files = dir.listFiles(fileFilter);

            ArrayList<TestSuite> tempList = new ArrayList<TestSuite>();
            for (int i = 0; i < files.length; i++) {
                String name = files[i].getName();
                name = name.substring(0, name.indexOf(Z7FileUtils.TESTSUITE_FILE_EXTENSION));
                TestSuite ts = new TestSuite();
                ts.setName(name);
                TestSuite ts2 = sTestSuiteMap.containsValue(ts);
                if (ts2 != null) {
                    ts.setChekedForExecution(ts2.isChekedForExecution());
                }
                tempList.add(ts);
            }

            Collections.sort(tempList, new Comparator<TestSuite>() {
                public int compare(TestSuite ts1, TestSuite ts2) {
                    return ts1.getName().toLowerCase().compareTo(ts2.getName().toLowerCase());
                }
            });

            sTestSuiteMap.clear();
            for (int i = 0; i < tempList.size(); i++) {
                sTestSuiteMap.put(i, tempList.get(i));
            }

            mListViewTests.setAdapter(new TestsAdapter(this));
            mListViewTests.setSelectionFromTop(sFirstVisibleItem, sTop);

            this.setTitle("Test suites on SD card, selected = " + sTestSuiteMap.getSelectedItemsCount());

        } catch (Exception e) {
            Log.e(LOG, e.getMessage(), e);
        }
    }

    /**
     * TestsAdapter.
     */
    class TestsAdapter extends ArrayAdapter<Object> {
        private Activity mContext;

        TestsAdapter(Activity context) {
            super(context, R.layout.options, sTestSuiteMap.values().toArray());
            this.mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Log.v(LOG, "TestsAdapter.getView()");
            LayoutInflater li = LayoutInflater.from(mContext);
            View row = li.inflate(R.layout.listitem2, null);
            CheckedTextView ctv = (CheckedTextView) row.findViewById(R.id.list_content2);
            ctv.setText(sTestSuiteMap.get(position).getName());
            ctv.setChecked(sTestSuiteMap.get(position).isChekedForExecution());
            return (row);
        }
    }

    // Create a message handling object as an anonymous class.
    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

            CheckedTextView ctvRequestOptions = (CheckedTextView) v.findViewById(R.id.list_content2);
            ctvRequestOptions.setChecked(!ctvRequestOptions.isChecked());
            sTestSuiteMap.get(position).setChekedForExecution(!sTestSuiteMap.get(position).isChekedForExecution());

            Intent returnIntent = new Intent();
            returnIntent.putExtra("loadTestSuiteFromSd", sTestSuiteMap.get(position).getName());
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG, "onPause()");

        markSelection();

        if (mAlertDialogDeleteTest != null) {
            if (mAlertDialogDeleteTest.isShowing()) {
                mAlertDialogDeleteTest.dismiss();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG, "onResume()");
        refreshTestSuites();
    }

    protected static final int CONTEXTMENU_LOADITEM = 0;
    protected static final int CONTEXTMENU_DELETEITEM = 1;
    private OnCreateContextMenuListener mOnCreateContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(sTestSuiteMap.get(((AdapterContextMenuInfo) menuInfo).position).getName());
            menu.add(0, CONTEXTMENU_LOADITEM, 0, "Load test suite");
            menu.add(0, CONTEXTMENU_DELETEITEM, 1, "Delete test suite");
            /* Add as many context-menu-options as you want to. */
        }
    };

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        /* Switch on the ID of the item, to get what the user selected. */
        switch (item.getItemId()) {
            case CONTEXTMENU_LOADITEM:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("loadTestSuiteFromSd", sTestSuiteMap.get(menuInfo.position).getName());
                setResult(RESULT_OK, returnIntent);
                finish();
                break;
            case CONTEXTMENU_DELETEITEM:
            /* Get the selected item out of the Adapter by its position. */
                final String testSuiteName = sTestSuiteMap.get(menuInfo.position).getName();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Are you sure you want to delete `" + testSuiteName + "` test suite?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteTestSuiteFromSd(testSuiteName);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                mAlertDialogDeleteTest = builder.create();
                mAlertDialogDeleteTest.show();
                break;
            default:
                return false;
        }
        return true;
    }

    private void deleteTestSuiteFromSd(String testSuiteName) {

        markSelection();

        if (Z7FileUtils.deleteTestSuiteFromSd(testSuiteName)) {
            refreshTestSuites();
            Toast.makeText(this, "Test suite" + testSuiteName + " was deleted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could not delete " + testSuiteName + " test suite!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add("itmRefresh");
        item.setTitle(R.string.itmRefresh);
        item.setOnMenuItemClickListener(mMenuItemRefresh);

        item = menu.add("itmSelectAll");
        item.setTitle("Select all");
        item.setOnMenuItemClickListener(mMenuItemSelectAll);

        item = menu.add("itmUnSelectAll");
        item.setTitle("Unselect all");
        item.setOnMenuItemClickListener(mMenuItemUnSelectAll);

        return true;
    }

    private MenuItem.OnMenuItemClickListener mMenuItemRefresh = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            refreshTestSuites();
            return true;
        }
    };

    private MenuItem.OnMenuItemClickListener mMenuItemSelectAll = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {

            for (int i = 0; i < sTestSuiteMap.size(); i++) {
                TestSuite ts = sTestSuiteMap.get(i);
                ts.setChekedForExecution(true);
                sTestSuiteMap.put(i, ts);
            }

            markSelection();

            refreshTestSuites();
            return true;
        }
    };

    private void markSelection() {
        sFirstVisibleItem = mListViewTests.getFirstVisiblePosition();
        View v = mListViewTests.getChildAt(0);
        sTop = (v == null) ? 0 : v.getTop();
    }

    private MenuItem.OnMenuItemClickListener mMenuItemUnSelectAll = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {

            for (int i = 0; i < sTestSuiteMap.size(); i++) {
                TestSuite ts = sTestSuiteMap.get(i);
                ts.setChekedForExecution(false);
                sTestSuiteMap.put(i, ts);
            }

            markSelection();

            refreshTestSuites();
            return true;
        }
    };
}
