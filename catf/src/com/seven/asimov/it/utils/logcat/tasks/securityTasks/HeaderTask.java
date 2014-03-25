package com.seven.asimov.it.utils.logcat.tasks.securityTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.HeaderWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderTask extends Task<HeaderWrapper> {

    private static final String TAG = HeaderTask.class.getSimpleName();
    private List<String> headerList;
    private static final String HEADER_REGEXP = "([A-Z]?)/Asimov::JNI::OCEngine\\(\\s?[0-9]+\\): ";
    private static final String END_HEADER_REGEXP = ": ([0-9]+|[-?,a-z]+)";
    private Pattern headerPattern;
    private Matcher matcher;
    private List<Pattern> patterns = new ArrayList<Pattern>();

    public HeaderTask(List<String> headerList) {
        this.headerList = headerList;
        compilePatterns();
    }

    public HeaderTask(String... headerList) {
        this.headerList = Arrays.asList(headerList);
        compilePatterns();
    }

//    @Override
//    protected HeaderWrapper parseLine(String line) {
//        for (String header : headerList){
//            headerPattern = Pattern.compile(HEADER_REGEXP + header, Pattern.CASE_INSENSITIVE);
////            headerPattern = Pattern.compile(HEADER_REGEXP + header + END_HEADER_REGEXP, Pattern.CASE_INSENSITIVE);
//            matcher = headerPattern.matcher(line);
//            if (matcher.find()){
//                HeaderWrapper wrapper = new HeaderWrapper();
//                wrapper.setLogLevel(matcher.group(1));
//                wrapper.setHeader(header);
////                wrapper.setValue(matcher.group(2));
//                return wrapper;
//            }
//        }
//        return null;
//    }

    @Override
    protected HeaderWrapper parseLine(String line) {
        for (int i = 0; i < patterns.size(); i++) {
            matcher = patterns.get(i).matcher(line);
            if (matcher.find()){
                HeaderWrapper wrapper = new HeaderWrapper();
                wrapper.setLogLevel(matcher.group(1));
                wrapper.setHeader(headerList.get(i));
                wrapper.setValue(matcher.group(2));
                return wrapper;
            }
        }
        return null;
    }

    private void compilePatterns(){
        for (String header : headerList){
            patterns.add(Pattern.compile(HEADER_REGEXP + header + END_HEADER_REGEXP, Pattern.CASE_INSENSITIVE ));
        }
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
