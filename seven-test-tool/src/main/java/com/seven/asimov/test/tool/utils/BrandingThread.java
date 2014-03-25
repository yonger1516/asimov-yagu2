package com.seven.asimov.test.tool.utils;

import java.util.concurrent.Callable;

public class BrandingThread implements Callable<Integer> {
    private String path;

    public BrandingThread(String path) {
        this.path = path;
    }

    @Override
    public Integer call() throws Exception {
        return BrandingLoaderUtil.addNewBranding(path);
    }
}
