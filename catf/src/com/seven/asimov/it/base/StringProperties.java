package com.seven.asimov.it.base;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Class for working with string properties.<br>
 * String properties list is a string of form "prop1=someValue1;prop2=someValue2;flag1;prop3=someValue3". <br>
 * Each property has form <code>name=value</code> or just <code>name</code>. <br>
 * Default delimiter is semicolon ";".
 * @author msvintsov
 *
 */
public abstract class StringProperties {

    public static final String DELIMITER = ";";
    
    /**
     * Returns true if property name exists in list of properties. <br>
     * @param property Property name to search
     * @param delim Delimiter
     * @return
     */
    public static boolean hasProperty(String properties, String name, String delim) {
        if (properties == null || name == null) return false;
        StringTokenizer st = new StringTokenizer(properties, delim);
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            int index = s.indexOf('=');
            String key = null;
            if (index < 0) {
                key = s;
            } else {
                key = s.substring(0, index);
            }
            if (key.equals(name)) return true;
        }
        return false;
    }
    
    public static boolean hasProperty(String properties, String name) {
        return hasProperty(properties, name, DELIMITER);
    }
    
    /**
     * Sets property to list of properties (overwrites if exists).
     * @param properties Initial list
     * @param name Name of property to set
     * @param value Value to set (if not <code>null</code>, property appears like <code>name=value</code>, if
     *          <code>null</code> - like <code>name</code>)
     * @param delim Delimiter
     * @return Modified list of properties
     */
    public static String setProperty(String properties, String name, String value, String delim) {
        if (properties == null) properties = "";
        String p = removeProperty(properties, name, delim);
        StringBuilder sb = new StringBuilder(p);
        if (p.length() != 0) sb.append(delim);
        sb.append(name);
        if (value != null) {
            sb.append('=');
            sb.append(value);
        }
        return sb.toString();
    }
    
    public static String setProperty(String properties, String name, String value) {
        return setProperty(properties, name, value, DELIMITER);
    }
    
    /**
     * Returns property value.
     * @param properties properties
     * @param name Name of property to search
     * @param delim Delimiter
     * @return Property value, or <code>null</code> id property is flag or does not exist
     */
    public static String getProperty(String properties, String name, String delim) {
        if (properties == null || name == null) return null;
        StringTokenizer st = new StringTokenizer(properties, delim);
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            int index = s.indexOf('=');
            String key = null;
            if (index < 0) {
                key = s;
            } else {
                key = s.substring(0, index);
            }
            if (key.equals(name)) {
                String value = null;
                if (index >= 0) {
                    value = s.substring(index+1);
                }
                return value;
            }
        }
        return null;
    }
    
    public static String getProperty(String properties, String name) {
        return getProperty(properties, name, DELIMITER);
    }
    
    /**
     * Removes property from list.
     * @param properties Initial list
     * @param name Name of property to remove
     * @param delim Delimiter
     * @return Modified list of properties
     */
    public static String removeProperty(String properties, String name, String delim) {
        if (properties == null || name == null) return properties;
        StringTokenizer st = new StringTokenizer(properties, delim);
        StringBuilder sb = new StringBuilder();
        boolean firstProp = true;
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            int index = s.indexOf('=');
            String key = null;
            if (index < 0) {
                key = s;
            } else {
                key = s.substring(0, index);
            }
            if (!key.equals(name)) {
                if (firstProp) {
                    firstProp = false;
                } else {
                    sb.append(delim);
                }
                sb.append(s);
            }
        }
        return sb.toString();
    }
    
    public static String removeProperty(String properties, String name) {
        return removeProperty(properties, name, DELIMITER);
    }

    /**
     * Converts properties string to map.
     * @param properties List of properties
     * @param delim Delimiter
     * @return Map String-String, flag properties have <code>null</code> values; 
     */
    public static Map<String, String> propertiesToMap(String properties, String delim) {
        Map<String, String> map= new HashMap<String, String>();
        if (properties == null) return map;
        StringTokenizer st = new StringTokenizer(properties, delim);
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            int index = s.indexOf('=');
            String key = null;
            if (index < 0) {
                key = s;
            } else {
                key = s.substring(0, index);
            }
            String value = null;
            if (index >= 0) {
                value = s.substring(index+1);
            }
            map.put(key, value);
        }
        return map;
    }
    
    public static Map<String, String> propertiesToMap(String properties) {
        return propertiesToMap(properties, DELIMITER);
    }
}
