package com.seven.asimov.test.tool.controls;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.seven.asimov.test.tool.activity.MainTab;
import com.seven.asimov.test.tool.activity.RootTab;
import com.seven.asimov.test.tool.activity.Tabs;
import com.seven.asimov.test.tool.core.TestFactory;
import org.apache.commons.lang.StringUtils;

/**
 * NewEditText - extends classic EditText.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class NewEditText extends EditText {

    public NewEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    private Context mContext;

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // User has pressed Back key. So hide the keyboard
            InputMethodManager mgr = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);

            this.clearFocus();

        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            // Eat the event
            return super.onKeyPreIme(keyCode, event);
        }
        return true;
    }

    private void displayTestFactoryInfo() {
        if (RootTab.getCurrentTab() == Tabs.MAIN_TAB) {
            Intent intent = new Intent(MainTab.ACTION_DISPLAY);
            intent.putExtra(TestFactory.MESSAGE, StringUtils.EMPTY);
            broadcastIntent(intent);
        }
    }

    private void broadcastIntent(Intent intent) {
        // Log.v(LOG, "displayIntent()");
        mContext.sendBroadcast(intent);
    }
}
