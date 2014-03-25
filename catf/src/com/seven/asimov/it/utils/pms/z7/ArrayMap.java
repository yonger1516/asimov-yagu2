package com.seven.asimov.it.utils.pms.z7;
//#ifndef J2ME

import com.seven.asimov.it.exception.z7.Z7Error;
import com.seven.asimov.it.utils.StringUtil;

import java.io.Serializable;
import java.util.*;


/**
 * A simple implementation of Map, using a single resizable array as its storage.
 * This class is very well suited for representing small Maps, where the overhead
 * of hashing outweighs the cost of a linear search. This class also minimizes
 * memory usage compared to HashMap.
 */

public class ArrayMap implements Cloneable, Serializable {

    protected static final int MINIMUM_CAPACITY      = 10;
    protected static final int KEY_NOT_FOUND         = -1;

    protected Object[] m_keys;
    protected Object[] m_values;
    protected int m_count;

    /** Create a new empty ArrayMap, with a default capacity. */
    public ArrayMap() {
        this(MINIMUM_CAPACITY);
    }

    /**
     * Create a new empty ArrayMap.
     * @param capacity the number of mappings this object can initially hold
     */
    public ArrayMap(int capacity) {
        if (capacity < MINIMUM_CAPACITY) capacity = MINIMUM_CAPACITY;
        m_keys = new Object[capacity];
        m_values = new Object[capacity];
        m_count = 0;
    }

    /**
     * Create a new ArrayMap, initialized with the contents of the
     * specified Map. Note: the copy is a shallow copy; see deepClone().
     * @param map the Map to copy
     */
    public ArrayMap(ArrayMap map) {
        this(map.size());
        // Use special uncheckedAddAll() method for speed.
        // Since this map is empty and data comes from another ArrayMap,
        // key uniqueness is guaranteed.
        uncheckedAddAll(map);
    }

    /** Removes all mappings from this map. */
    public void clear() {
        // First release all references that we might have in the array, to free up heap
        for (int i = m_count; --i >= 0; ) {
            m_keys[i] = null;
            m_values[i] = null;
        }
        m_count = 0;
    }

    /** Returns the number of key-value mappings in this map. */
    public int size() { return m_count; }

    /** Returns true if this map contains no mappings. */
    public boolean isEmpty() { return m_count == 0; }

    /** Returns a string representation of the Map suitable for logging. */
    public String toString() {
        StringBuffer buf = new StringBuffer(500);
        toString(buf);
        return buf.toString();
    }

    public void toString(StringBuffer buf) {
        buf.append('{');
        final int count = m_count;
        for (int i = 0; i < count; ++i) {
            if (i != 0) {
                buf.append(',').append(' ');
            }
            StringUtil.appendObjectToStringBuffer(m_keys[i], buf);
            buf.append('=');
            StringUtil.appendObjectToStringBuffer(m_values[i], buf);
        }
        buf.append('}');
    }


    /* Returns a shallow copy; the keys and values themselves are not cloned. See deepClone(). */
    public Object clone() {
        ArrayMap m = new ArrayMap(m_count);
        // Use special uncheckedAddAll() method for speed.
        // Since the new map is empty and data is copied from another ArrayMap,
        // key uniqueness is guaranteed.
        m.uncheckedAddAll(this);
        return m;
    }

    /** Returns the key at the specified index. */
    public Object getKeyAt(int i) { return m_keys[i]; }

    /** Sets the key at the specified index. Returns the previous key. */
    public Object putKeyAt(int i, Object key) {
        Object o = m_keys[i];
        m_keys[i] = key;
        return o;
    }

    /** Returns the value at the specified index. */
    public Object getAt(int i) { return m_values[i];  }

    /** Returns the value at the specified index. Returns the previous value. */
    public Object putAt(int i, Object value) {
        Object o = m_values[i];
        m_values[i] = value;
        return o;
    }

    public Object removeAt(int i) {
        Object result = null;
        result = m_values[i];

        // If this is an element from the middle of the
        // array and we need to shift the arrays.
        int diff = m_count - i - 1;
        if ( diff > 0 )
        {
            System.arraycopy(m_keys, i + 1, m_keys, i, diff);
            System.arraycopy(m_values, i + 1, m_values, i, diff);
        }

        --m_count;
        m_keys[m_count] = null;
        m_values[m_count] = null;

        return result;
    }

    /** Returns the value to which this map maps the specified key. */
    public Object get(Object key) {
        int i = indexOfKey(key);
        if (i == KEY_NOT_FOUND) return null;
        return m_values[i];
    }
    public  boolean getBoolean(Object key, boolean defaultValue) {
        Object o = get(key);
        if (!(o instanceof Boolean)) return defaultValue;
        return ((Boolean)o).booleanValue();
    }
    public int getInt(Object key, int defaultValue) {
        Object o = get(key);
        if (!(o instanceof Integer)) return defaultValue;
        return ((Integer)o).intValue();
    }
    public short getShort(Object key, short defaultValue) {
        Object o = get(key);
        if (!(o instanceof Short)) return defaultValue;
        return ((Short)o).shortValue();
    }
    public long getLong(Object key, long defaultValue) {
        Object o = get(key);
        if (!(o instanceof Long)) return defaultValue;
        return ((Long)o).longValue();
    }
    public String getString(Object key, String defaultValue) {
        Object o = get(key);
        if (!(o instanceof String)) return defaultValue;
        return (String)o;
    }
    public String getString(Object key) {
        return getString(key, null);
    }
    public Date getDate(Object key) {
        Object o = get(key);
        if (!(o instanceof Date)) return null;
        return (Date)o;
    }
    public byte[] getBytes(Object key) {
        Object o = get(key);
        if (!(o instanceof byte[])) return null;
        return (byte[])o;
    }
    public List getList(Object key) {
        Object o = get(key);
        if (!(o instanceof List)) return null;
        return (List)o;
    }
    public ArrayMap getArrayMap(Object key) {
        Object o = get(key);
        if (!(o instanceof ArrayMap)) return null;
        return (ArrayMap)o;
    }
    public IntArrayMap getIntArrayMap(Object key) {
        Object o = get(key);
        if (!(o instanceof IntArrayMap)) return null;
        return (IntArrayMap)o;
    }
    public Z7Error getError(Object key) {
        Object o = get(key);
        if (!(o instanceof Z7Error)) return null;
        return (Z7Error)o;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * @return the previous value for this key, or null if no previous value.
     */
    public Object put(Object key, Object val) {
        return put(key, val, false);
    }
    public Object put(Object key, Object val, boolean convertCollisionToList) {
        if (key == null) throw new NullPointerException();
        int i = indexOfKey(key);
        if (i != KEY_NOT_FOUND) {
            // key already exists, replace value
            Object result = m_values[i];
            if (convertCollisionToList) {
                if (result != null && result instanceof List) {
                    ((List)result).add(val);
                } else {
                    ArrayList list = new ArrayList();
                    list.add(result);
                    list.add(val);
                    m_values[i] = list;
                }
            } else {
                m_values[i] = val;
            }
            return result;
        } else {
            growIfNeeded(1);
            m_keys[m_count] = key;
            m_values[m_count++] = val;
            return null;
        }
    }

    /**
     * If the map cannot hold <code>nExtraValues</code> more key/value pair(s), grow it.
     */
    protected void growIfNeeded(int nExtraValues) {
        final int neededCapacity = m_count + nExtraValues;
        if (neededCapacity > m_keys.length) {

            // This size calculation is also used by recent ArrayList implementations.
            int newSize = ((m_count * 3) >> 1) + 1;
            if (newSize < neededCapacity) {
                newSize = neededCapacity;
            }

            System.arraycopy(m_keys, 0, (m_keys = new Object[newSize]), 0, m_count);
            System.arraycopy(m_values, 0, (m_values = new Object[newSize]), 0, m_count);
        }
    }

    /**
     * Associates the specified value with the specified key in this map. If the value
     * is null, does nothing.
     * @return the previous value for this key, or null if no previous value.
     */
    public Object putIfNotNull(Object key, Object val) {
        return (val == null) ? null : put(key, val);
    }

    /** Copies all of the mappings from the specified map to this map */
    public void putAll(ArrayMap m) {
        final int count = m.size();
        for (int i = 0; i < count; ++i) {
            put(m.m_keys[i], m.m_values[i]);
        }
    }

    /** Removes the mapping for this key from this map if present */
    public Object remove(Object key) {
        int i = indexOfKey(key);
        if ( i == KEY_NOT_FOUND ) {
            return null;
        }
        return removeAt( i );
    }

    /** Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(Object key) {
        return indexOfKey(key) != KEY_NOT_FOUND;
    }

    /** Returns true if this map maps one or more keys to the specified value. */
    public boolean containsValue(Object val) {
        final boolean isValNull = (val == null);
        for (int i = m_count; --i >= 0; ) {
            Object tmp = m_values[i];
            if (isValNull) {
                return (tmp == null);
            } else if (val.equals(tmp)) {
                return true;
            }
        }
        return false;
    }

    /** Returns a list of all of the keys in the map. */
    public List getKeys() {
        List result = new ArrayList();
        final int count = m_count;
        for (int i = 0; i < count; ++i) {
            result.add(m_keys[i]);
        }
        return result;
    }
    /** Returns a list of all of the values in the map. */
    public List getValues() {
        List result = new ArrayList();
        final int count = m_count;
        for (int i = 0; i < count; ++i) {
            result.add(m_values[i]);
        }
        return result;
    }

    /** Returns a "deep" clone of the map; all values are themselves cloned if required. Not all object types are supported. */
    public ArrayMap deepClone() {
        final int count = m_count;
        ArrayMap clone = new ArrayMap(count);
        for (int i = 0; i < count; ++i) {
            Object key = m_keys[i];
            Object keyClone;
            if (key instanceof String) {
                // common case, save a method call for immutable String
                keyClone = key;
            } else {
                keyClone = deepCloneObject(key);
            }
            Object val = m_values[i];
            Object valClone;
            if (val == null || val instanceof String || val instanceof Integer || val instanceof Boolean) {
                // Save a method call for "common" immutable objects
                valClone = val;
            } else {
                valClone = deepCloneObject(val);
            }
            // Use special uncheckedAdd() method for speed.
            // Since the new map starts out empty and data comes from another ArrayMap,
            // key uniqueness is guaranteed.
            clone.uncheckedAdd(keyClone, valClone);
        }
        return clone;
    }

    /** Returns null if the maps are equal, otherwise a String indicating where the difference was found */
    public String deepEquals(Object o) {
        return deepEqualsObject(this, o);
    }

    public static Object deepCloneObject(Object obj) {
        Object clone;
        if ((obj == null) ||
                (obj instanceof String) ||
                (obj instanceof Boolean) ||
                (obj instanceof Integer)) {
            // common immutable objects
            clone = obj;
        } else if (obj instanceof List) {
            List lst = (List)obj;
            int lstSize = lst.size();
            final List lstClone = new ArrayList(lstSize);
            for (int i = 0; i < lstSize; ++i) {
                lstClone.add(deepCloneObject(lst.get(i)));
            }
            clone = lstClone;
        } else if (obj instanceof ArrayMap) {
            clone = ((ArrayMap)obj).deepClone();
        } else if (obj instanceof IntArrayMap) {
            clone = ((IntArrayMap)obj).deepClone();
        } else if (obj instanceof byte[]) {
            byte [] b = (byte [])obj;
            clone = new byte[b.length];
            System.arraycopy(b, 0, (byte[]) clone, 0, b.length);
        } else if (obj instanceof Date) {
            Date d = (Date)obj;
            clone = new Date(d.getTime());
        } else if (obj instanceof Z7Error) {
            Z7Error err = (Z7Error)obj;
            clone = new Z7Error(err.getErrorCode(), err.getResultCode(), err.getDescription(), err.getNestedError());
        } else if (obj instanceof Throwable) {
            clone = obj; // assume immutable
        } else if ((obj instanceof Short) ||
                (obj instanceof Byte) ||
                (obj instanceof Long) ||
                (obj instanceof Character)) {
            // less common immutable objects
            clone = obj;
        } else {
            //m_logger.error("deepClone not supported for: " + obj.getClass().getName(), new Throwable());
            clone = null;
        }
        return clone;
    }

    public static String deepEqualsObject(Object o1, Object o2) {
        if (o1 == o2) return null;
        if (o1 == null || o2 == null) return "Null object";
        if (o1.getClass() != o2.getClass()) return "Class (" + o1.getClass().getName() + " vs " + o2.getClass().getName() + ")";
        if ((o1 instanceof String) ||
                (o1 instanceof Boolean) ||
                (o1 instanceof Date) ||
                (o1 instanceof Short) ||
                (o1 instanceof Byte) ||
                (o1 instanceof Long) ||
                (o1 instanceof Character) ||
                (o1 instanceof Integer)) {
            if (!o1.equals(o2)) return o1.getClass().getName() + " (" + o1 + " vs " + o2 + ")";
        } else if (o1 instanceof List) {
            List lst1 = (List)o1;
            List lst2 = (List)o2;
            final int size1 = lst1.size();
            int size2 = lst2.size();
            if (size1 != size2) return "List size (" + size1 + " vs " + size2 + ")";
            for (int i = 0; i < size1; ++i) {
                String eq = deepEqualsObject(lst1.get(i), lst2.get(i));
                if (eq != null) return "List entry[" + i + "]: " + eq;
            }
        } else if (o1 instanceof ArrayMap) {
            ArrayMap map1 = (ArrayMap)o1;
            ArrayMap map2 = (ArrayMap)o2;
            final int size1 = map1.size();
            int size2 = map2.size();
            if (size1 != size2) return "ArrayMap size (" + size1 + " vs " + size2 + ")";
            for (int i = 0; i < size1; ++i) {
                String eq = deepEqualsObject(map1.m_keys[i], map2.m_keys[i]);
                if (eq != null) return "ArrayMap key[" + i + "]: " + eq;
                eq = deepEqualsObject(map1.m_values[i], map2.m_values[i]);
                if (eq != null) return "ArrayMap value[@" + i + "='" + map1.m_keys[i] + "']: " + eq;
            }
        } else if (o1 instanceof IntArrayMap) {
            IntArrayMap map1 = (IntArrayMap)o1;
            IntArrayMap map2 = (IntArrayMap)o2;
            final int size1 = map1.size();
            int size2 = map2.size();
            if (size1 != size2) return "IntArrayMap size (" + size1 + " vs " + size2 + ")";
            for (int i = 0; i < size1; ++i) {
                int key1 = map1.m_keys[i];
                int key2 = map2.m_keys[i];
                if (key1 != key2) return "IntArrayMap key[" + i + "] (" + key1 + " vs " + key2 + ")";
                String eq = deepEqualsObject(map1.m_values[i], map2.m_values[i]);
                if (eq != null) return "IntArrayMap value[@" + i + "=" + map1.m_keys[i] + "]: " + eq;
            }
        } else if (o1 instanceof byte[]) {
            byte [] b1 = (byte [])o1;
            byte [] b2 = (byte [])o2;
            final int size1 = b1.length;
            int size2 = b2.length;
            if (size1 != size2) return "byte[] size (" + size1 + " vs " + size2 + ")";
            for (int i = 0; i < size1; ++i) {
                if (b1[i] != b2[i]) return "byte[] value[" + i + "] (" + b1[i] + " vs " + b2[i] + ")";
            }
        } else if (o1 instanceof Z7Error) {
            Z7Error ex1 = (Z7Error)o1;
            Z7Error ex2 = (Z7Error)o2;
            if (ex1.getErrorCode() != ex2.getErrorCode()) return "Z7Error (" + ex1.getErrorCode() + " vs " + ex2.getErrorCode() + ")";
        } else {
            //m_logger.error("deepequals not supported for: " + o1.getClass().getName(), new Throwable());
            return "not supported";
        }
        return null;
    }

    /** Returns the index of the specified key in the Map, or KEY_NOT_FOUND if the key is not found. */
    protected int indexOfKey(Object key) {
        if (key != null) {  // short circuit on null key
            for (int i = m_count; --i >= 0; ) {
                if (key.equals(m_keys[i]))
                    return i;
            }
        }
        return KEY_NOT_FOUND;
    }


    /**
     * <b>WARNING</b>: Does not check for duplicate keys.
     * <p>
     * Adds the specified key/value pair to the end of this map.
     * <p>
     * This method is O(1), whereas {@link #put(Object, Object)} is O(N). But the increased
     * performance comes at a cost. There is no checking for duplicate keys, so this method must
     * only be used when using a key that is known to not exist in this map.
     * <p>
     * This method is most suitable for deserializing the entries in an ArrayMap.
     *
     */
    public void uncheckedAdd(Object key, Object val) {
        if (key == null) {
            throw new NullPointerException();
        }
        growIfNeeded(1);
        m_keys[m_count] = key;
        m_values[m_count++] = val;
    }

    /**
     * <b>WARNING</b>: Does not check for duplicate keys.
     * <p>
     * Adds the key/value pairs in the specified <code>srcMap</code> to the end of this map.
     * <p>
     * This method is O(M), whereas {@link #putAll(ArrayMap)} is O(M*(M+N)),
     * where M is the size of the specified <code>srcMap</code> and N is the size of this map.
     * But the increased performance comes at a cost. There is no checking for duplicate keys,
     * so this method must only be used when it is known that the specified <code>srcMap</code>
     * has no keys already in this map.
     * <p>
     * This method is most suitable for initializing a newly constructed ArrayMap from another
     * ArrayMap.
     *
     * @see #putAll(ArrayMap)
     */
    public void uncheckedAddAll(ArrayMap srcMap) {
        final int nExtraValues = srcMap.m_count;
        growIfNeeded(nExtraValues);
        System.arraycopy(srcMap.m_keys, 0, m_keys, m_count, nExtraValues);
        System.arraycopy(srcMap.m_values, 0, m_values, m_count, nExtraValues);
        m_count += nExtraValues;
    }

    public Map toHashMap() {
        Map map = new HashMap();
        List keys = getKeys();
        for(int i = 0; i < keys.size(); i++) {
            Object key = keys.get(i);
            map.put(key, get(key));
        }
        return map;
    }

    public int hashCode() {
        return this.toHashMap().hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return this.toHashMap().equals(((ArrayMap)obj).toHashMap());
    }

}
