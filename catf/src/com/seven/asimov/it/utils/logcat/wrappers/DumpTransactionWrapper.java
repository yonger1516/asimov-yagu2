package com.seven.asimov.it.utils.logcat.wrappers;

public class DumpTransactionWrapper extends LogEntryWrapper{

    private static final String TAG = DumpTransactionWrapper.class.getSimpleName();

    private String direction;
    private int bytes;

    public String getDirection(){
        return direction;
    }

    public void setDirection(String direction){
        this.direction = direction;
    }

    public int getBytes(){
        return bytes;
    }

    public void setBytes(int bytes){
        this.bytes = bytes;
    }

    @Override
    protected String getTAG() {return TAG;};

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" direction is ").append(getDirection()).toString();
    }
    /*
    enum DIRECTION{
        REQUEST,
        RESPONSE
    }
    */
}
