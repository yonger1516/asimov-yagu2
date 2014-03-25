package com.seven.asimov.it.tests.stability.base;

import com.seven.asimov.it.base.constants.BaseConstantsIF;
import com.seven.asimov.it.testcases.StabilityTestCase;
import com.seven.asimov.it.utils.PrepareResourceUtil;

import java.io.FileWriter;

public class StabilityMemoryLeakHttpSizeLoadTests extends StabilityTestCase {

    public void test_001_StabilityMemoryHttpSize() throws Throwable {
        String name = "Http";
        FileWriter[] fileWriters = new FileWriter[5];
        fileWriters[0] = new FileWriter(BaseConstantsIF.SD_CARD + "/OCIntegrationTestsResults/003_HttpMemoryLeak.txt");
        fileWriters[1] = new FileWriter(BaseConstantsIF.SD_CARD + "/OCIntegrationTestsResults/003_HttpsMemoryLeak.txt");
        fileWriters[2] = new FileWriter(BaseConstantsIF.SD_CARD + "/OCIntegrationTestsResults/003_DNSMemoryLeak.txt");
        fileWriters[3] = new FileWriter(BaseConstantsIF.SD_CARD + "/OCIntegrationTestsResults/003_OCCMemoryLeak.txt");
        fileWriters[4] = new FileWriter(BaseConstantsIF.SD_CARD + "/OCIntegrationTestsResults/003_EngineMemoryLeak.txt");
        int[] stepTime = {1, 1, 1};
        String resource = "asimov_stability_memory_http_size";
        String uri = createTestResourceUri(resource);
        try {
            memoryLeakSimulation(name, fileWriters, stepTime, uri, true);
        } finally {
            for (FileWriter fileWriter : fileWriters)
                fileWriter.close();
            PrepareResourceUtil.invalidateResourceSafely(uri);
            cleanTempList();
            if (!exceptions.isEmpty()) {
                Throwable t = exceptions.get(0);
                exceptions.clear();
                throw t;
            }
        }
    }

}