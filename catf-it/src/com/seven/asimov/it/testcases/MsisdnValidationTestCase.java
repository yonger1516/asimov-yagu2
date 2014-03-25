package com.seven.asimov.it.testcases;

import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.utils.OCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsisdnValidationTestCase extends AsimovTestCase {

    private static final Logger logger = LoggerFactory.getLogger(MsisdnValidationTestCase.class.getSimpleName());

    protected void reinstallOcc(String appPath) throws Exception {
        logger.trace("Before removeOCClient");
        OCUtil.removeOCClient();
        logger.trace("After removeOCClient");
        logger.trace("Before installOCClient");
        OCUtil.installOCClientWithVerifying(appPath);
        logger.trace("After installOCClient");
    }
}
