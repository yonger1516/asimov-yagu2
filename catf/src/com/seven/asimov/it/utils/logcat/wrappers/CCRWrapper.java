package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: akaliev
 * Date: 10/10/13
 * Time: 6:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class CCRWrapper extends LogEntryWrapper {
   private String fck;
    private int codeVerdict;
    private  String stringVerdict;

    @Override
    public String toString() {
        return super.toString()+" CCRWrapper{" +"fck='" + fck + '\'' +", codeVerdict=" + codeVerdict +", stringVerdict='" + stringVerdict + '\'' +'}';
    }

    public String getFck() {
        return fck;
    }

    public void setFck(String fck) {
        this.fck = fck;
    }

    public int getCodeVerdict() {
        return codeVerdict;
    }

    public void setCodeVerdict(int codeVerdict) {
        this.codeVerdict = codeVerdict;
    }

    public String getStringVerdict() {
        return stringVerdict;
    }

    public void setStringVerdict(String stringVerdict) {
        this.stringVerdict = stringVerdict;
    }
}
