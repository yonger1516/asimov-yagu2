package com.seven.asimov.it.utils.logcat.wrappers;

public class FcVerificationWrapper extends LogEntryWrapper {

    private int verificationResult;

    public FcVerificationWrapper(int verificationResult,long timestamp) {
        this.verificationResult = verificationResult;
        setTimestamp(timestamp);
    }

       public int getVerificationResult() {
        return verificationResult;
    }

    public void setVerificationResult(int verificationResult) {
        this.verificationResult = verificationResult;
    }

    @Override
    public String toString(){
        StringBuilder result = new StringBuilder("FcVerification : [VerificationResult= ");
        result.append(verificationResult);
        result.append("]");
        return result.toString();
    }
}
