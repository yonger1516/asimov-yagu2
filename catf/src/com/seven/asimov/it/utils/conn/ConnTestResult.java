package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.utils.conn.TcpConnection.ConnType;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnTestResult {

    public enum StateCode {
        NONE,
        CONNECTING,
        CONNECTED,
        CONNECT_FAILED,
        ACCEPTED,
        DATA_RECEIVED,
        FIN_RECEIVED,
        RST_RECEIVED,
        DATA_SENT,
        FIN_SENT,
        RST_SENT,
        SEND_FAILED,
        DONE;
    }

    public static final int CONN_ID_NEW = -1;

    private class ConnTestRecord {
        private int mConnId;
        private ConnType mConnType;
        private StateCode mCode;

        public ConnTestRecord(int connId, ConnType type, StateCode code) {
            mConnId = connId;
            mConnType = type;
            mCode = code;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder()
                    .append(mConnId).append(" : ")
                    .append(mConnType).append(" : ")
                    .append(mCode);
            return sb.toString();
        }
    }

    private ArrayList<ConnTestRecord> mRecords;
    private String mReason;
    private final Lock mLock = new ReentrantLock();

    public ConnTestResult() {
        mRecords = new ArrayList<ConnTestRecord>();
    }

    public void addRecord(int connId, ConnType type, StateCode code) {
        ConnTestRecord record = new ConnTestRecord(connId, type, code);
        addRecord(record);
    }

    public void setReason(String reason) {
        this.mReason = reason;
    }

    public String getReason() {
        return this.mReason;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mReason != null && mReason.length() > 0) {
            sb.append("Reason:");
            sb.append(mReason);
            sb.append("\n");
        }

        sb.append("Records:\n");

        try {
            mLock.lock();
            for (ConnTestRecord record : mRecords) {
                sb.append("   ");
                sb.append(record.toString())
                        .append("\n");
            }
        } finally {
            mLock.unlock();
        }
        return sb.toString();
    }

    public void addRecord(ConnTestRecord record) {
        try {
            mLock.lock();
            mRecords.add(record);
        } finally {
            mLock.unlock();
        }
    }
}
