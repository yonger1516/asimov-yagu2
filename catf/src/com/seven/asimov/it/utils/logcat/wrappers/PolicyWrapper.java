package com.seven.asimov.it.utils.logcat.wrappers;

public class PolicyWrapper extends LogEntryWrapper {
    private static final String TAG = PolicyWrapper.class.getSimpleName();

    private int id;
    private String name;
    private String value;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(PolicyWrapper otherPolicy){
        if (otherPolicy == null)
            return false;
        if (this.id != otherPolicy.id)
            return false;
        if (!this.name.equals(otherPolicy.name))
            return false;
        if (!this.value.equals(otherPolicy.value))
            return false;
        return true;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" id: ").append(getId()).append(" name: ").append(getName()).append(" value: ").append(getValue()).toString();
    }

}
