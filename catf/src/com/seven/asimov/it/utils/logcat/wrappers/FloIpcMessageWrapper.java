package com.seven.asimov.it.utils.logcat.wrappers;

public class FloIpcMessageWrapper extends LogEntryWrapper {
    private String message;
    private int actionFLO;
    private int optionsFLO;
    private int typeIPC;
    private int chainIdIPC;

    public int getTypeIPC() {
        return typeIPC;
    }

    public void setTypeIPC(String typeIPC) {
        this.typeIPC = Integer.parseInt(typeIPC);
    }

    public int getChainIdIPC() {
        return chainIdIPC;
    }

    public void setChainIdIPC(String chainIdIPC) {
        this.chainIdIPC = Integer.parseInt(chainIdIPC);
    }

    public int getOptionsFLO() {
        return optionsFLO;
    }

    public void setOptionsFLO(String optionsFLO) {
        this.optionsFLO = Integer.parseInt(optionsFLO);
    }

    public int getActionFLO() {
        return actionFLO;
    }

    public void setActionFLO(String actionFLO) {
        this.actionFLO = Integer.parseInt(actionFLO);
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
