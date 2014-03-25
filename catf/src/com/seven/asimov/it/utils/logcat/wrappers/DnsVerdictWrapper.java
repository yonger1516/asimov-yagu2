package com.seven.asimov.it.utils.logcat.wrappers;

public class DnsVerdictWrapper extends LogEntryWrapper {
    private String transactionId;
    private String verdict;

    public DnsVerdictWrapper(String transactionId, String verdict) {
        this.transactionId = transactionId;
        this.verdict = verdict;
    }

    public DnsVerdictWrapper() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DnsVerdictWrapper that = (DnsVerdictWrapper) o;

        if (!transactionId.equals(that.transactionId)) return false;
        return verdict.equals(that.verdict);
    }

    @Override
    public int hashCode() {
        int result = transactionId.hashCode();
        result = 31 * result + verdict.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("DnsVerdictWrapper: [transactionId = %s, verdict = %s]", transactionId, verdict);
    }
}
