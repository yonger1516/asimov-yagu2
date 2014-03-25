package com.seven.asimov.it.utils.logcat.wrappers;

public class DefaultConfigReInstalledWrapper extends LogEntryWrapper{

    private static final String TAG = DefaultConfigReInstalledWrapper.class.getSimpleName();
    private boolean configDeleted;
    private boolean configInstalled;

    public DefaultConfigReInstalledWrapper() {
    }

    public boolean isConfigDeleted() {
        return configDeleted;
    }

    public void setConfigDeleted(boolean configDeleted) {
        this.configDeleted = configDeleted;
    }

    public boolean isConfigInstalled() {
        return configInstalled;
    }

    public void setConfigInstalled(boolean configInstalled) {
        this.configInstalled = configInstalled;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(", configDeleted=").append(configDeleted)
                .append(", configInstalled").append(configInstalled).toString();
    }
}
