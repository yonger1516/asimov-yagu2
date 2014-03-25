package com.seven.asimov.it.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.seven.asimov.it.utils.ScreenUtils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Screen management utility 
 * @author opissarev
 * 
 */
public class ScreenUtils {

	private static final String Screenoff = Intent.ACTION_SCREEN_OFF;
	private static final String Screenon = Intent.ACTION_SCREEN_ON;
	private static AtomicInteger refCount = new AtomicInteger(0);
	private static Object lock = new Object();
	
	private static Map<ScreenSpyResult, ScreenSpyReceiver> screenSpyReceivers = new HashMap<ScreenUtils.ScreenSpyResult, ScreenSpyReceiver>();
	
	public static void screenOff() {
		sendBroadcastMessage(Screenoff);
	}
	
	public static void screenOn() {
		sendBroadcastMessage(Screenon);
	}
	
	/**
	 * Starts listening of SCREEN_ON / SCREEN_OFF actions. <br>
	 * Do not forget to call {@link #finishScreenSpy(Context, ScreenSpyResult)}
	 * in <code>finally</code> block of the test.
	 * @param c Context
	 * @param spy Object that contains info about last received action
	 */
	public static void startScreenSpy(Context c, ScreenSpyResult spy) {
	    System.out.println("Starting Screen Spy");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        ScreenSpyReceiver r = new ScreenSpyReceiver(spy);
        screenSpyReceivers.put(spy, r);
        c.registerReceiver(r, intentFilter);
	}
	
	/**
     * Starts listening of SCREEN_ON / SCREEN_OFF actions, and switches screen on / off. <br>
     * Do not forget to call {@link #finishScreenSpy(Context, ScreenSpyResult)}
     * in <code>finally</code> block of the test.
     * @param c Context
     * @param screenOn
     */
	public static ScreenSpyResult switchScreenAndSpy(Context c, boolean screenOn) {
        ScreenSpyResult spy = new ScreenSpyResult(screenOn);
        synchronized (lock) {
            ScreenUtils.startScreenSpy(c, spy);
            refCount.addAndGet(1);
            if (screenOn) {
                ScreenUtils.screenOn();
            } else {
                ScreenUtils.screenOff();
            }    
        }        
        ScreenUtils.waitFor(spy);
        TestUtil.sleep(3000);
        return spy;
    }
	
	/**
	 * Finish listening of SCREEN_ON / SCREEN_OFF actions.
	 * @param c
	 * @param spy
	 * @return 
	 */
	public static int finishScreenSpy(Context c, ScreenSpyResult spy) {
	    int count = refCount.decrementAndGet();
	    System.out.println("Finishing Screen Spy (still active spy count is " + count + ")");
	    try {
           ScreenSpyReceiver r = screenSpyReceivers.remove(spy);
           if (r != null) c.unregisterReceiver(r);
	    } catch (Exception e) {
            e.printStackTrace();
        }
	    return count;
    }
	
	/**
     * Finish listening of SCREEN_ON / SCREEN_OFF actions. If screen was off, switches it on
     * @param c
     * @param spy
     */
    public static void finishSpyAndResetScreen(Context c, ScreenSpyResult spy) {
        if (spy == null) return;
        try {
            int referenceCount = ScreenUtils.finishScreenSpy(c, spy);
            // reset screen to normal (On) mode if it was Off during test
            synchronized (lock) {
                if (referenceCount == 0 && !spy.expectedScreenOn) {
                    ScreenUtils.screenOn();
                    TestUtil.sleep(3000);
                }
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static void waitFor(ScreenSpyResult spy) {
	    for (int i=0; i<5; i++) {
	        try {
                Thread.sleep(5000);
                if (spy.isScreenAsExpected()) return;
            } catch (InterruptedException e) { }
	    }
	}
	
	/*public static boolean isScreenOn(Context c) {
	    PowerManager pm = (PowerManager)
	    c.getSystemService(Context.POWER_SERVICE);
	    boolean isScreenOn = pm.isScreenOn();
	    return isScreenOn;
	}*/
	

	private static void sendBroadcastMessage(String msg){
		try {
			TestUtil.doCmd("/system/bin/am broadcast -a " + msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	/**
	 * Class that contains info about last received SCREEN_ON / SCREEN_/OFF action. <br>
	 * Last action is reflected in value of <code>screenOn</code>:
	 * <ul>
	 *     <li><code>null</code> - no action received yet</li>
	 *     <li><code>true</code> - last received action is Intent.ACTION_SCREEN_ON</li>
	 *     <li><code>false</code> - last received action is Intent.ACTION_SCREEN_OFF</li>
	 * </ul>
	 * @author msvintsov
	 *
	 */
	public static class ScreenSpyResult {
	    
	    public ScreenSpyResult(boolean expectedScreenOn) {
	        this.expectedScreenOn = expectedScreenOn;
	    }
	    
	    private Boolean screenOn;
	    private boolean expectedScreenOn;
	    
	    public synchronized boolean isScreenAsExpected() {
	        return screenOn != null && screenOn.equals(expectedScreenOn);
	    }
	    

	    protected synchronized void setScreenOn(Boolean screenOn) {
            this.screenOn = screenOn;
        }

	}

}

class ScreenSpyReceiver extends BroadcastReceiver {
    
    private ScreenSpyResult spyResult;
    
    ScreenSpyReceiver(ScreenSpyResult screenSpyResult) {
        this.spyResult = screenSpyResult;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            spyResult.setScreenOn(true);
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            spyResult.setScreenOn(false);
        }
        System.out.println("Screen Spy: Action received: " + intent.getAction());
    }
    
}



