package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * @author Rushchak Y.
 */
public class SocketError extends LogEntryWrapper {

       private ErrorType errorType;

       public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public String toString(){
        StringBuilder result = new StringBuilder("Time=");
        result.append(getTimestamp());
        result.append("\t");
        result.append("ErrorType=");
        result.append(errorType.toString());
        return result.toString();
    }

    public static enum ErrorType{
        IN_SOCKET_ERROR("OUT socket error"),
        OUT_SOCKET_ERROR("IN socket error"),
        OUT_SOCKET_CLOSED("OUT socket closed by peer");

        private String name;
        ErrorType(String name){
            this.name = name;
        }
    }
}
