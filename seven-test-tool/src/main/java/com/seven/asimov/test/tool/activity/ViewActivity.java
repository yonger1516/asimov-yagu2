package com.seven.asimov.test.tool.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.seven.asimov.test.tool.R;
import com.seven.asimov.test.tool.view.FolderLayout;
import com.seven.asimov.test.tool.view.IFolderItemListener;

import java.io.File;

public class ViewActivity extends Activity implements IFolderItemListener {
    FolderLayout localFolders;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folders);

        String menuName = getIntent().getExtras().getString("Key_name");
        localFolders = (FolderLayout) findViewById(R.id.localfolders);
        localFolders.setIFolderItemListener(this);
        localFolders.setMenuName(menuName);
        localFolders.setDir("./sdcard");
        setTitle(AutomationTestsTab.isFileAdded() ? R.string.sSelectOC : R.string.addBranding);
    }

    public void OnCannotFileRead(File file) {
        new AlertDialog.Builder(this)
                .setTitle(
                        "[" + file.getName()
                                + "] folder can't be read!")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();

    }

    public void OnFileClicked(File file) {
        final File f = file;
        new AlertDialog.Builder(this)
                .setTitle("[" + file.getName() + "]")
                .setMessage("Do you want add " + (AutomationTestsTab.isFileAdded() ? "apk " : "branding ") +
                        file.getName() + "?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = getIntent();
                                i.putExtra("path", f.getPath());
                                setResult(RESULT_OK, i);
                                finish();
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .show();
    }
}