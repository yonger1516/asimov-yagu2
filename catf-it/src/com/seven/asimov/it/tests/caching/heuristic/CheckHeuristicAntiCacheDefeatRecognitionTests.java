package com.seven.asimov.it.tests.caching.heuristic;


import com.seven.asimov.it.testcases.HeuristicCachingTestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CheckHeuristicAntiCacheDefeatRecognitionTests extends HeuristicCachingTestCase {

    public void testRandomNumberNormalization() throws Exception {

        final String resourceUri = createTestResourceUri(RESOURCE) + "_rnn/index.php";
        final String resourcePattern = resourceUri + "?key=%s";
        final String addition = String.format(resourcePattern, random.nextInt(100));
        testHeuristicAntiCacheDefeatRecognition(resourceUri, resourcePattern, addition);
    }

    public void test_001_DateValueNormalization() throws Exception {

        final String resourceUri = createTestResourceUri(RESOURCE) + "_dvn/index.php";
        final String resourcePattern = resourceUri + "?key=%s";

        final long minTime = 1356480000000L;
        final int timeRange = 1728000000;
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String addition = String.format(resourcePattern, dateFormat.format(new Date(random.nextInt(timeRange) + minTime)));

        testHeuristicAntiCacheDefeatRecognition(resourceUri, resourcePattern, addition);
    }

    public void test_002_KeyWithLettersNoNormalization() throws Exception {

        final String resourceUri = createTestResourceUri(RESOURCE) + "_lnn/index.php";
        final String resourcePattern = resourceUri + "?key=%s";
        final String addition = String.format(resourcePattern, String.valueOf(random.nextInt(100))
                + (char) (random.nextInt(ASCII_LETTERS_RANGE) + FIRST_ASCII_LETTER_CODE));

        testHeuristicAntiCacheDefeatRecognition(resourceUri, resourcePattern, addition);
    }
}
