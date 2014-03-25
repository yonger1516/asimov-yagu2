package com.seven.asimov.test.tool.serialization;

import com.seven.asimov.test.tool.validation.VerificationPattern;
import org.apache.commons.lang.StringUtils;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TestSuite - serializable class.
 *
 * @author Maksim Selivanov
 */

@Root
public class TestSuite {

    private static final Logger LOG = LoggerFactory.getLogger(TestSuite.class.getSimpleName());

    @Element
    public int id;

    @Element
    public String name;

    @Element(required = false)
    public String pattern;

    @Element(required = false)
    public String verificationPattern;

    @Element(required = false)
    public String belongs;

    @Element(required = false)
    public String scope;

    @ElementList(name = "tests", required = false)
    public List<TestItem> tests;

    public TestSuite() {
        tests = new ArrayList<TestItem>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    private static final String DEFAULT_PATTERN = "45";

    public ArrayList<String> getIntervalPattern() {

        ArrayList<String> result = new ArrayList<String>();

        if (!StringUtils.isEmpty(getPattern())) {
            result.addAll(Arrays.asList(getPattern().replace(".", ",").split(",")));
        } else {
            result.add(DEFAULT_PATTERN);
        }
        return result;
    }

    public int getIntervalPatternSize() {
        return getIntervalPattern().size();
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    private static int sCurrentIntervalId;

    public void moveToNextInterval() {
        if (getIntervalPatternSize() == (sCurrentIntervalId + 1)) {
            if (getIntervalPatternSize() > 1) {
                sCurrentIntervalId = 1;
            } else {
                sCurrentIntervalId = 0;
            }
        } else {
            sCurrentIntervalId++;
        }
    }

    public void resetIntervalPattern() {
        sCurrentIntervalId = 0;
    }

    public int getCurrentIntervalId() {
        return sCurrentIntervalId;
    }

    public void moveToPreviousInterval() {
        if (sCurrentIntervalId - 1 > 0) {
            sCurrentIntervalId--;
        } else {
            sCurrentIntervalId = getIntervalPatternSize() - 1;
        }
    }

    public String getInitialDelayAndMoveToNext() {
        String result = null;
        if (getIntervalPatternSize() > 1 && sCurrentIntervalId == 0) {
            result = getIntervalPattern().get(sCurrentIntervalId);
            moveToNextInterval();
            return result;
        }
        return result;
    }

    public String getCurrentIntervalAndMoveToNext() {

        LOG.debug("Pattern: " + Arrays.toString(getIntervalPattern().toArray()));
        LOG.debug("Pattern size: " + getIntervalPatternSize());
        LOG.debug("Current interval id: " + sCurrentIntervalId);

        String currPattern = getIntervalPattern().get(sCurrentIntervalId);
        LOG.debug("Current interval: " + currPattern);

        if (currPattern != null) {

            moveToNextInterval();

            LOG.debug("Next interval id: " + (sCurrentIntervalId));
            LOG.debug("Next interval: " + getIntervalPattern().get(sCurrentIntervalId));
        }

        return currPattern;
    }

    public VerificationPattern getVerificationPattern() {
        return mVPattern;
    }

    public void setVerificationPattern(String verificationPattern) {
        this.verificationPattern = verificationPattern;
    }

    private VerificationPattern mVPattern;

    public void parseVerificationPattern() {
        this.mVPattern = new VerificationPattern(verificationPattern);
    }

    public String getBelongs() {
        return belongs;
    }

    public void setBelongs(String belongs) {
        this.belongs = belongs;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<TestItem> getTestItems() {
        return tests;
    }

    private boolean mChekedForExecution;

    public void setChekedForExecution(boolean chekedForExecution) {
        this.mChekedForExecution = chekedForExecution;
    }

    public boolean isChekedForExecution() {
        return mChekedForExecution;
    }

    private Boolean mValidatedOk;

    public void setValidatedOk(Boolean validatedOk) {
        this.mValidatedOk = validatedOk;
    }

    public Boolean isValidatedOk() {
        return mValidatedOk;
    }
}
