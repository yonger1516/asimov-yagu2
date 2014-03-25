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
public class IntArrayMap implements Cloneable, Serializable {

    /**
     * The value must be regenerated every time class structure changes.
     */
    private static final long serialVersionUID = -3079288546162386069L;

    protected static final int MINIMUM_CAPACITY       = 10;
    protected static final int KEY_NOT_FOUND          = -1;

    protected int [] m_keys;
    protected Object[] m_values;
    protected int m_count;

    /** Create a new empty ArrayMap, with a default capacity. */
    public IntArrayMap() {
        this(MINIMUM_CAPACITY);
    }

    /**
     * Create a new empty ArrayMap.
     * @param capacity the number of mappings this object can initially hold
     */
    public IntArrayMap(int capacity) {
        if (capacity < MINIMUM_CAPACITY) capacity = MINIMUM_CAPACITY;
        m_keys = new int[capacity];
        m_values = new Object[capacity];
        m_count = 0;
    }

    /**
     * Create a new IntArrayMap, initialized with the contents of the
     * specified Map. Note: the copy is a shallow copy; see deepClone().
     * @param map the Map to copy
     */
    public IntArrayMap(IntArrayMap map) {
        this(map == null ? MINIMUM_CAPACITY : map.size());
        if (map != null) {
            // Use special uncheckedAddAll() method for speed.
            // Since this map is empty and data comes from another IntArrayMap,
            // key uniqueness is guaranteed.
            uncheckedAddAll(map);
        }
    }

    /** Removes all mappings from this map. */
    public void clear() {
        // First release all references that we might have in the array, to free up heap
        for (int i = m_count; --i >= 0; ) {
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
            buf.append(m_keys[i]).append('=');
            StringUtil.appendObjectToStringBuffer(m_values[i], buf);
        }
        buf.append('}');
    }

    /* Returns a shallow copy; the keys and values themselves are not cloned. See deepClone(). */
    public Object clone() {
        IntArrayMap m = new IntArrayMap(m_count);
        // Use special uncheckedAddAll() method for speed.
        // Since the new map is empty and data is copied from another IntArrayMap,
        // key uniqueness is guaranteed.
        m.uncheckedAddAll(this);
        return m;
    }

    /** Returns the key at the specified index. */
    public int getKeyAt(int i) { return m_keys[i]; }

    /** Sets the key at the specified index. Returns the previous key. */
    public int putKeyAt(int i, int key) {
        int oldKey = m_keys[i];
        m_keys[i] = key;
        return oldKey;
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
        Object result = m_values[i];

        // If this is an element from the middle of the
        // array and we need to shift the arrays.
        int diff = m_count - i - 1;
        if ( diff > 0 )
        {
            System.arraycopy(m_keys, i + 1, m_keys, i, diff);
            System.arraycopy(m_values, i + 1, m_values, i, diff);
        }

        --m_count;
        m_keys[m_count] = 0;
        m_values[m_count] = null;

        return result;
    }

    /** Returns the value to which this map maps the specified key. */
    public Object get(int key) {
        int i = indexOfKey(key);
        if (i == KEY_NOT_FOUND) return null;
        return m_values[i];
    }
    public  boolean getBoolean(int key, boolean defaultValue) {
        Object o = get(key);
        if (!(o instanceof Boolean)) return defaultValue;
        return ((Boolean)o).booleanValue();
    }
    public int getInt(int key, int defaultValue) {
        Object o = get(key);
        if (o instanceof Integer){
            return ((Integer) o).intValue();
        } else if (o instanceof Short){
            return (int)(((Short) o).shortValue());
        } else if (o instanceof Byte){
            return (int)(((Byte) o).byteValue());
        }
        return defaultValue;
    }
    public short getShort(int key, short defaultValue) {
        Object o = get(key);
        if (o instanceof Short)
            return ((Short)o).shortValue();
        if (o instanceof Byte)
            return (short)(((Byte) o).byteValue());
        return defaultValue;
    }
    public long getLong(int key, long defaultValue) {
        Object o = get(key);
        if (o instanceof Long)
            return ((Long)o).longValue();
        else if (o instanceof Integer)
            return (long)(((Integer) o).intValue());
        else if (o instanceof Short)
            return (long)(((Short) o).shortValue());
        else if (o instanceof Byte)
            return (long)(((Byte) o).byteValue());
        return defaultValue;
    }
    public byte getByte(int key, byte defaultValue) {
        Object o = get(key);
        if (o instanceof Byte)
            return ((Byte) o).byteValue();
        return defaultValue;
    }
    public String getString(int key, String defaultValue) {
        Object o = get(key);
        if (!(o instanceof String)) return defaultValue;
        return (String)o;
    }
    public String getString(int key) {
        return getString(key, null);
    }
    public Date getDate(int key) {
        Object o = get(key);
        if (!(o instanceof Date)) return null;
        return (Date)o;
    }
    public byte[] getBytes(int key) {
        Object o = get(key);
        if (!(o instanceof byte[])) return null;
        return (byte[])o;
    }
    public List getList(int key) {
        Object o = get(key);
        if (!(o instanceof List)) return null;
        return (List)o;
    }
    public ArrayMap getArrayMap(int key) {
        Object o = get(key);
        if (!(o instanceof ArrayMap)) return null;
        return (ArrayMap)o;
    }
    public IntArrayMap getIntArrayMap(int key) {
        Object o = get(key);
        if (!(o instanceof IntArrayMap)) return null;
        return (IntArrayMap)o;
    }
    public Z7Error getError(int key) {
        Object o = get(key);
        if (!(o instanceof Z7Error)) return null;
        return (Z7Error)o;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * @return the previous value for this key, or null if no previous value.
     */
    public Object put(int key, Object val) {
        int i = indexOfKey(key);
        if (i != KEY_NOT_FOUND) {
            // key already exists, replace value
            Object oldValue = m_values[i];
            m_values[i] = val;
            return oldValue;
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

            System.arraycopy(m_keys, 0, (m_keys = new int[newSize]), 0, m_count);
            System.arraycopy(m_values, 0, (m_values = new Object[newSize]), 0, m_count);
        }
    }

    /**
     * Associates the specified value with the specified key in this map. If the value
     * is null, does nothing.
     * @return the previous value for this key, or null if no previous value.
     */
    public Object putIfNotNull(int key, Object val) {
        return (val == null) ? null : put(key, val);
    }

    /** Copies all of the mappings from the specified map to this map */
    public void putAll(IntArrayMap m) {
        final int count = m.size();
        for (int i = 0; i < count; i++) {
            put(m.m_keys[i], m.m_values[i]);
        }
    }

    /** Removes the mapping for this key from this map if present */
    public Object remove(int key) {
        int i = indexOfKey(key);
        if ( i == KEY_NOT_FOUND ) {
            return null;
        }
        return removeAt( i );
    }

    /** Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(int key) {
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

    /**
     *@return an array of int keys
     */
    public int[] getKeys() {
        if (m_count == 0)
            return null;
        int [] keys = new int[m_count];
        System.arraycopy(m_keys, 0, keys, 0, m_count);
        return keys;
    }

    /** Returns a list of all of the values in the map. */
    public List getValues() {
        final int count = m_count;
        List result = new ArrayList(count);
        for (int i = 0; i < count; ++i) {
            result.add(m_values[i]);
        }
        return result;
    }

    /** Returns a "deep" clone of the map; all values are themselves cloned if required. Not all object types are supported. */
    public IntArrayMap deepClone() {
        final int count = m_count;
        IntArrayMap clone = new IntArrayMap(count);
        for (int i = 0; i < count; i++) {
            Object val = m_values[i];
            Object valClone;
            if (val == null || val instanceof String || val instanceof Integer || val instanceof Boolean) {
                // Save a method call for "common" immutable objects
                valClone = val;
            } else {
                valClone = ArrayMap.deepCloneObject(val);
            }
            // Use special uncheckedAdd() method for speed.
            // Since the new map starts out empty and data comes from another IntArrayMap,
            // key uniqueness is guaranteed.
            clone.uncheckedAdd(m_keys[i], valClone);
        }
        return clone;
    }

    /** Returns null if the maps are equal, otherwise a String indicating where the difference was found */
    public String deepEquals(Object o) {
        return ArrayMap.deepEqualsObject(this, o);
    }

    /** Returns the index of the specified key in the Map, or KEY_NOT_FOUND if the key is not found. */
    protected int indexOfKey(int key) {
        for (int i = m_count; --i >= 0; ) {
            if (key == m_keys[i]) {
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
     * This method is O(1), whereas {@link #put(int, Object)} is O(N). But the increased performance
     * comes at a cost. There is no checking for duplicate keys, so this method must only be used
     * when using a key that is known to not exist in this map.
     * <p>
     * This method is most suitable for deserializing the entries in an IntArrayMap.
     *
     * @see #put(int, Object)
     */
    public void uncheckedAdd(int key, Object val) {
        growIfNeeded(1);
        m_keys[m_count] = key;
        m_values[m_count++] = val;
    }

    /**
     * <b>WARNING</b>: Does not check for duplicate keys.
     * <p>
     * Adds the key/value pairs in the specified <code>srcMap</code> to the end of this map.
     * <p>
     * This method is O(M), whereas {@link #putAll(IntArrayMap)} is O(M*(M+N)),
     * where M is the size of the specified <code>srcMap</code> and N is the size of this map.
     * But the increased performance comes at a cost. There is no checking for duplicate keys,
     * so this method must only be used when it is known that the specified <code>srcMap</code>
     * has no keys already in this map.
     * <p>
     * This method is most suitable for initializing a newly constructed IntArrayMap from another
     * IntArrayMap.
     *
     * @see #putAll(IntArrayMap)
     */
    public void uncheckedAddAll(IntArrayMap srcMap) {
        final int nExtraValues = srcMap.m_count;
        growIfNeeded(nExtraValues);
        System.arraycopy(srcMap.m_keys, 0, m_keys, m_count, nExtraValues);
        System.arraycopy(srcMap.m_values, 0, m_values, m_count, nExtraValues);
        m_count += nExtraValues;
    }

    public Map toHashMap() {
        Map map = new HashMap();
        for(int i = 0; i < m_keys.length; i++) {
            int key = m_keys[i];
            map.put(new Integer(key), get(key));
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
        return this.toHashMap().equals(((IntArrayMap)obj).toHashMap());
    }

}
