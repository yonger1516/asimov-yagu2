package com.seven.asimov.test.tool.activity;

import java.util.ArrayList;
import java.util.List;

public class HeaderInfo {
    private String name;
    private List<TestInfo> productList = new ArrayList<TestInfo>();

    public HeaderInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestInfo> getProductList() {
        return productList;
    }

    public void setProductList(List<TestInfo> productList) {
        this.productList = productList;
    }

    public void addTestInfo(TestInfo testInfo) {
        productList.add(testInfo);
    }

    public static class TestInfo {
        private String testName;
        private String information;

        public TestInfo(String testName) {
            this.testName = testName;
        }

        public TestInfo(String testName, String information) {
            this.information = information;
            this.testName = testName;
        }

        public String getInformation() {
            return information;
        }

        public void setInformation(String information) {
            this.information = information;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

    }
}
