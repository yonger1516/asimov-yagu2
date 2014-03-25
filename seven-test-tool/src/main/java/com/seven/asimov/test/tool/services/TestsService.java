package com.seven.asimov.test.tool.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TestsService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
