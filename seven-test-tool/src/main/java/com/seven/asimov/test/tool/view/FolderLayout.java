package com.seven.asimov.test.tool.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.seven.asimov.test.tool.R;

import java.io.File;
import java.util.*;

public class FolderLayout extends LinearLayout implements AdapterView.OnItemClickListener {
    Context context;
    IFolderItemListener folderListener;
    private List<String> path = null;
    private String root = "./sdcard";
    private TextView myPath;
    private ListView lstView;
    private String menuName = "";

    public FolderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.folderview, this);

        myPath = (TextView) findViewById(R.id.path);
        lstView = (ListView) findViewById(R.id.list);

        Log.i("FolderView", "Constructed");
        getDir(root, lstView);

    }

    public void setIFolderItemListener(IFolderItemListener folderItemListener) {
        this.folderListener = folderItemListener;
    }

    public void setDir(String dirPath) {
        getDir(dirPath, lstView);
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    private void getDir(String dirPath, ListView v) {
        v.getSelectedItemPosition();
        myPath.setText("Location: " + dirPath);
        List<String> item = new ArrayList<String>();
        path = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();
        List<File> listOfFile = Arrays.asList(files);
        Collections.sort(listOfFile, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        if (!dirPath.equals(root)) {
            item.add(root);
            path.add(root);
            item.add("../");
            path.add(f.getParent());
        }
        for (File file : listOfFile) {
            if (file.isDirectory()) {
                path.add(file.getPath());
                item.add(file.getName() + "/");
            }
        }
        for (File file : listOfFile) {
            if (!file.isDirectory()) {
                if (menuName.equals("branding")) {
                    if (file.getName().contains(".target")) {
                        path.add(file.getPath());
                        item.add(file.getName());
                    }
                } else if (menuName.equals("property")) {
                    if (file.getName().contains(".properties")) {
                        path.add(file.getPath());
                        item.add(file.getName());
                    }
                } else if (menuName.equals("select")) {
                    if (file.getName().contains(".apk")) {
                        path.add(file.getPath());
                        item.add(file.getName());
                    }
                }
            }
        }
        Log.i("Folders", files.length + "");
        setItemList(item);
    }

    public void setItemList(List<String> item) {
        ArrayAdapter<String> fileList = new ListViewAdapter(context, item);
        lstView.setAdapter(fileList);
        lstView.setOnItemClickListener(this);
    }

    public void onListItemClick(ListView l, int position) {

        File file = new File(path.get(position));
        if (file.isDirectory()) {
            if (file.canRead()) {
                getDir(path.get(position), l);
            } else {
                if (folderListener != null) {
                    folderListener.OnCannotFileRead(file);
                }
            }
        } else {
            if (folderListener != null) {
                folderListener.OnFileClicked(file);
            }
        }
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        onListItemClick((ListView) arg0, arg2);
    }

}
