package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: kam
 * Date: 10/22/13
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class CheckUpdatePolligWrapper extends LogEntryWrapper {
    String PollingEnabled;
    String CheckPending;
    String NetworkConnectivity;

    public String getPollingEnabled() {
        return PollingEnabled;
    }

    public void setPollingEnabled(String pollingEnabled) {
        PollingEnabled = pollingEnabled;
    }

    public String getCheckPending() {
        return CheckPending;
    }

    public void setCheckPending(String checkPending) {
        CheckPending = checkPending;
    }

    public String getNetworkConnectivity() {
        return NetworkConnectivity;
    }

    public void setNetworkConnectivity(String networkConnectivity) {
        NetworkConnectivity = networkConnectivity;
    }

    public String getWaitRoamingStop() {
        return WaitRoamingStop;
    }

    public void setWaitRoamingStop(String waitRoamingStop) {
        WaitRoamingStop = waitRoamingStop;
    }

    String WaitRoamingStop;
    long timer;


    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return super.toString()+" CheckUpdatePolligWrapper{" +
                "PollingEnabled=" + PollingEnabled +
                ", CheckPending=" + CheckPending +
                ", NetworkConnectivity=" + NetworkConnectivity +
                ", WaitRoamingStop=" + WaitRoamingStop +
                ", timer=" + timer +
                '}';
    }
}
