package com.seven.asimov.it.utils.logcat.wrappers;

import java.util.ArrayList;
import java.util.List;

public class SpanHistoryWrapper extends LogEntryWrapper {

    List<SpanHistoryRecord> records = new ArrayList<SpanHistoryRecord>();

    public void addRecord(SpanHistoryRecord record) {
        records.add(record);
    }

    public int getSize() {
        return records.size();
    }

    public List<SpanHistoryRecord> getRecords() {
        return records;
    }

    public static class SpanHistoryRecord {

        private String requestInterval;

        public SpanHistoryRecord(String requestInterval) {
            this.requestInterval = requestInterval;
        }

        public String getRequestInterval() {
            return requestInterval;
        }

        @Override
        public String toString() {
            return "SpanHistoryRecord{" +
                    "requestInterval='" + requestInterval + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SpanHistoryWrapper{" +
                "records=" + records +
                '}';
    }
}