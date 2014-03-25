package com.seven.asimov.it.utils.pms.z7;

import com.seven.asimov.it.exception.z7.Z7Result;
import com.seven.asimov.it.utils.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

//#ifndef J2ME
//#else
//# import com.seven.compat.*;
//#endif

public class Z7ContactParameter {

    // On the native side the class is derived from a list but in
    // Java we cannot really do that so the class owns the list instead
    private List m_list = null;

    public Z7ContactParameter() {
        m_list = new ArrayList();
    }

    public Z7ContactParameter(List list) {
        m_list = list;
    }

    public Z7ContactParameter(Z7ContactParameter contactParameter) {
        this(contactParameter.m_list);
    }

    public Z7ContactParameter(String value) {
        this();
        add(value);
    }

    public List toList() {
        return m_list;
    }

    public int getSize() {
        return m_list.size();
    }

    public void add(String value) {
        m_list.add(value);
    }

    // TODO: int32 compare( const Z7Object& objectRef ) const;
    // TODO: virtual bool equals( const Z7Object& objectRef ) const { return compare( objectRef ) == 0; }

    public boolean equivalent(Z7ContactParameter param) {
        for (int i = 0; i < m_list.size(); i++)
            if (!param.hasValue((String) m_list.get(i)))
                return false;
        for (int i = 0; i < param.m_list.size(); i++)
            if (!hasValue((String) param.m_list.get(i)))
                return false;
        return true;
    }

    public Z7Result ensureValueExists(String value) {
        if (hasValue(value))
            return Z7Result.Z7_S_NOTHING_TO_DO;
        m_list.add(value);
        return Z7Result.Z7_OK;
    }

    public Z7Result removeValue(String value) {
        int index = m_list.indexOf(value);
        if (index == -1)
            return Z7Result.Z7_S_NOTHING_TO_DO;
        m_list.remove(index);
        return Z7Result.Z7_OK;
    }

    public boolean hasValue(String value) {
        return m_list.contains(value);
    }

    Z7Result serializeAll(OutputStream outStream) {
        try {
            int numOfParams = getSize();
            outStream.write(numOfParams);
            String value = null;
            byte[] bytes = null;
            int length = 0;
            for (int i = 0; i < numOfParams; i++) {
                value = (String) this.m_list.get(i);
                bytes = StringUtil.getUTF8Bytes(value);
                length = bytes.length;
                outStream.write(length);
                outStream.write(bytes);
            }
            return Z7Result.Z7_OK;
        } catch (IOException ioe) {
            return Z7Result.Z7_E_SERIALIZE_FAILURE;
        }
    }


    public static Z7ContactParameter createFromStream(InputStream inStream) {
        try {
            Z7ContactParameter contactParameter = new Z7ContactParameter();
            int numOfParams = inStream.read();
            int length = 0;
            byte[] bytes = null;
            String value = null;
            for (int i = 0; i < numOfParams; i++) {
                length = inStream.read();
                bytes = new byte[length];
                if (inStream.read(bytes) < length) {
                    return null;
                }
                value = new String(bytes, "UTF-8");
                contactParameter.add(value);
            }
            return contactParameter;
        } catch (UnsupportedEncodingException use) {
            return null;
        } catch (IOException ioe) {
            return null;
        }
    }
/*
    public String toString() {
        StringBuffer buffy = new StringBuffer();
        int size = m_list.size();
        for (int i=0; i<size; ++i) {
            buffy.append(m_list.get(i));
            if (i<size-1) {
                buffy.append(",");
            }
        }
        return buffy.toString();
    }
*/
}
