package com.seven.asimov.it.utils.logcat.wrappers;


public class VerdictForFCLWrapper extends LogEntryWrapper {
    String verdict = null;

    public void setVerdict(String verdict){
        this.verdict = verdict;
    }

    public String getVerdict(){
        return verdict;
    }

}
