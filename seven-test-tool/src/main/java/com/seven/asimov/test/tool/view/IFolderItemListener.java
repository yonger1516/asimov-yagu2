package com.seven.asimov.test.tool.view;

import java.io.File;

public interface IFolderItemListener {
    void OnCannotFileRead(File file);

    void OnFileClicked(File file);
}
