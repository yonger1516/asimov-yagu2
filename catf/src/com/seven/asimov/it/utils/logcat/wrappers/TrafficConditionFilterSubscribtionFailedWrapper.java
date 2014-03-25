package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 8/1/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrafficConditionFilterSubscribtionFailedWrapper extends LogEntryWrapper {
    private String filter;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).
                append(" filter: ").append(filter).toString();
    }

}
