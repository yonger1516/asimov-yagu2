package com.seven.asimov.test.tool.utils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parameters.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class Parameters {

    private ArrayList<Parameter> mParameterList = new ArrayList<Parameter>();

    public String get(String key) {
        Iterator<Parameter> entries = mParameterList.iterator();
        while (entries.hasNext()) {
            Parameter header = entries.next();
            if (header.getKey().equals(key)) {
                return header.getValue();
            }
        }
        return null;
    }

    public boolean put(String key, String value) {
        return mParameterList.add(new Parameter(key, value));
    }

    public boolean add(Parameter parameter) {
        return mParameterList.add(parameter);
    }

    public boolean remove(String key) {
        return mParameterList.remove(key);
    }

    public void clear() {
        mParameterList.clear();
    }

    public boolean removeIgnoreCase(String key) {
        boolean result = false;
        int n = 0;
        while (mParameterList.size() > 0) {
            Parameter p = mParameterList.get(n);
            if (p.getKey().equalsIgnoreCase(key)) {
                mParameterList.remove(n);
                result = true;
                n = 0;
            } else {
                n++;
            }
            if (n == mParameterList.size()) {
                break;
            }
        }
        return result;
    }

    public boolean containsKey(String key) {
        Iterator<Parameter> entries = mParameterList.iterator();
        while (entries.hasNext()) {
            Parameter header = entries.next();
            if (header.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsIgnoreCaseKey(String key) {
        Iterator<Parameter> entries = mParameterList.iterator();
        while (entries.hasNext()) {
            Parameter header = entries.next();
            if (header.getKey().equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    public Parameter[] toArray() {
        return mParameterList.toArray(new Parameter[mParameterList.size()]);
    }

    public String[] keySet() {
        ArrayList<String> keyList = new ArrayList<String>();
        Iterator<Parameter> entries = mParameterList.iterator();
        while (entries.hasNext()) {
            Parameter header = entries.next();
            keyList.add(header.getKey());
        }
        return keyList.toArray(new String[keyList.size()]);
    }
}
