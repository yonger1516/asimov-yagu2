package com.seven.asimov.it.utils.pms;

public class Policy {

    private String name;
    private String value;
    private String path;
    private boolean shouldBe;

    public Policy(String name, String value, String path, boolean shouldBe) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.shouldBe = shouldBe;
    }

    @Override
    public String toString() {
        return String.format("Policy: name = %s, value = %s, path = %s, shouldBe = %s ;", getName(), getValue(), getPath(), isShouldBe());
    }

    @Override
    public int hashCode() {
        return name.hashCode() + path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;

        Policy other = (Policy) obj;
        return getName().equals(other.getName()) && getPath().equals(other.getPath());
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getPath() {
        return path;
    }

    public boolean isShouldBe() {
        return shouldBe;
    }

    public static enum Policies {
        CACHE_INVALIDATE_AGGRESSIVENESS("@asimov@http"),
        ENABLED("@asimov"),
        NO_CACHE_INVALIDATE_AGGRESSIVENESS("@asimov@http"),
        OUT_OF_ORDER_AGGRESSIVENESS("@asimov@http"),
        RESPONSE_HEADER_RULES("@asimov@normalization@header@com.seven.asimov.it"),
        REQUEST_HEADER_RULES("@asimov@normalization@header@com.seven.asimov.it"),
        ROAMING_WIFI("@asimov@failovers"),
        TRANSPARENT("@asimov"),
        REPORT_NET_PROTO_STACK("@asimov@reporting@analysis");

        private Policies(String path){
            mPath = path;
        }

        public String getPath(){
            return mPath;
        }

        public String getName(){
            return toString().toLowerCase();
        }

        public Policy getPolicy (String value, boolean shouldBe){
            return new Policy(toString().toLowerCase(), value, mPath, shouldBe);
        }

        private String mPath;
    }
}