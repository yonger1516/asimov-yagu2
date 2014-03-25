package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 7/30/13
 * Time: 11:49 AM
 * To change this template use File | Settings | File Templates.
 */
public enum ConditionState {
    Disabled,
    Entered,
    Exited,
    Waiting_for_configuration {
        public String toString() {
            return "Waiting for configuration";
        }
    }
}
