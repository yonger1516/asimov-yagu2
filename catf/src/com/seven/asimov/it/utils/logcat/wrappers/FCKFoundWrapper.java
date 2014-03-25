package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: akaliev
 * Date: 10/9/13
 * Time: 8:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class FCKFoundWrapper extends LogEntryWrapper {
    private int id;
    private String fck;

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public void setFck(String fck) {
        this.fck = fck;
    }
    public String getFck() {
        return fck;
    }
    @Override
     public String toString() {
        return super.toString()+"FCKFoundWrapper{" +"id=" + id +", fck='" + fck + '}';
    }
}
