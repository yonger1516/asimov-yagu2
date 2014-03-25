package com.seven.asimov.it.utils.pms.z7;
//#ifndef J2ME

import com.seven.asimov.it.base.constants.Z7TransportConstantsIF;
import com.seven.asimov.it.utils.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

//#else
//# import com.seven.compat.*;
//#endif


/**
 * Represents an address for a host and/or instance in a 7TP network.
 */

/** PORT: native/transport/common/src/Z7Transport.h/.cpp */

public class Z7TransportAddress implements Serializable {

    public static final int Z7_TRANSPORT_POLICY_CONTROL_FUNCTION_HOSTID = 19;

    //High bits of host id and 1 byte for instance id
    private int m_nocIdInstanceId;
    //Low bits of host id
    private int m_hostId;
    // For convenience the complete host id, not serialized.
    private long m_hostIdAsLong;

    //
    // Cache formatted transport address for performance improvement
    //
    private String m_strFormattedAddress;

    /**
     * Constructs a newly allocated Z7TransportAddress with zero values.
     */
    public Z7TransportAddress() {
        reset();
    }

    public Z7TransportAddress(long hostId, byte instanceId) {
        m_nocIdInstanceId = ((int) ((0x00ffffff00000000l & hostId) >> 24)) | (0x000000ff & instanceId);
        m_hostId = (int) (0x00000000ffffffffl & hostId);
        m_hostIdAsLong = hostId;
        m_strFormattedAddress = formatToStringAddress();
    }


    /**
     * Constructs a newly allocated Z7TransportAddress from the specified component values.
     */
    public Z7TransportAddress(int nocId, long hostId, byte instanceId) {
        this(hostId,instanceId);
    }

    public Z7TransportAddress(int nocId, int hostId, byte instanceId) {
        this((0x00000000ffffffffl & hostId),instanceId);
    }


    /**
     * Constructs a newly allocated Z7TransportAddress from the raw bytes represented as a byte array, starting at offset.
     */
    public Z7TransportAddress(byte[] buffer, int offset) {
        initialize(buffer, offset);
        m_strFormattedAddress = formatToStringAddress();
    }
    public Z7TransportAddress(byte[] buffer) {
        initialize(buffer, 0);
        m_strFormattedAddress = formatToStringAddress();
    }

    /**
     * Z7TransportAddress copy constructor
     */
    public Z7TransportAddress(Z7TransportAddress addr) {
        m_nocIdInstanceId = addr.m_nocIdInstanceId;
        m_hostId = addr.m_hostId;
        m_hostIdAsLong = parseHostIdAsLong(m_nocIdInstanceId, m_hostId);
        m_strFormattedAddress = formatToStringAddress();
    }

    /**
     * Z7TransportAddress copy constructor with new local id.
     */
    public Z7TransportAddress(Z7TransportAddress addr, byte newInstanceId) {
        m_nocIdInstanceId = (addr.m_nocIdInstanceId & 0xffffff00) | (0x000000ff & newInstanceId);
        m_hostId = addr.m_hostId;
        m_hostIdAsLong = addr.m_hostIdAsLong;
        m_strFormattedAddress = formatToStringAddress();
    }

    public void reset() {
        m_nocIdInstanceId = Z7TransportConstantsIF.Z7_TRANSPORT_INSTANCE_ID_DEFAULT_ANY;
        m_hostId = 0;
        m_hostIdAsLong = 0;

        m_strFormattedAddress = null;
    }

    /**
     * Constructs a newly allocated Z7TransportAddress from the raw bytes represented as a byte array, starting at offset.
     */
    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(m_nocIdInstanceId);
        outputStream.writeInt(m_hostId);
    }

    public void deserialize(DataInputStream inputStream) {
        try {
            m_nocIdInstanceId = inputStream.readInt();
            m_hostId = inputStream.readInt();
            m_hostIdAsLong = parseHostIdAsLong(m_nocIdInstanceId, m_hostId);
            m_strFormattedAddress = formatToStringAddress();
        } catch (IOException e) {
           // m_logger.warn("Failed to deserialize",e);
        }
    }

    public void serialize(byte[] buffer, int offset) {
        StringUtil.writeInt(m_nocIdInstanceId, buffer, offset);
        StringUtil.writeInt(m_hostId, buffer, offset + 4);
    }

    public static int getSerialLength() {
        return 8;
    }

    /**
     * J2ME does not have ByteBuffer class.
     * Substitution for serialize( ByteBuffer or DataOutputStream ) 
     */
    public byte[] toByteArray() {
        byte[] byteVal = new byte[8];
        StringUtil.writeInt(m_nocIdInstanceId, byteVal, 0);
        StringUtil.writeInt(m_hostId, byteVal, 4);
        return byteVal;
    }

    /**
     * Indicates if two Z7TransportAddress objects represent the same Z7 host address.
     */
    public boolean equals(Object o) {
        if ( o instanceof Z7TransportAddress ) {
            Z7TransportAddress addr = (Z7TransportAddress)o;
            return ( (m_nocIdInstanceId == addr.m_nocIdInstanceId) && (m_hostId == addr.m_hostId) && (m_hostIdAsLong == addr.m_hostIdAsLong));
        }
        return false;
    }

    /**
     * Sets the Instance Id associated with the Z7TransportAddress.
     */
    public void setInstanceId(byte instanceId) {
        m_nocIdInstanceId = (m_nocIdInstanceId & 0xffffff00) | (0x000000ff & instanceId);
        m_hostIdAsLong = parseHostIdAsLong(m_nocIdInstanceId, m_hostId);
        m_strFormattedAddress = formatToStringAddress();
    }

    /**
     * Sets the address to be equals to another one.
     */
    public void setAddress(Z7TransportAddress addr) {
        m_nocIdInstanceId = addr.m_nocIdInstanceId;
        m_hostId = addr.m_hostId;
        m_hostIdAsLong = parseHostIdAsLong(m_nocIdInstanceId, m_hostId);
        m_strFormattedAddress = formatToStringAddress();
    }

    /**
     * Initializes a Z7TransportAddress from the raw bytes represented as a byte array, starting at offset.
     */
    public void initialize(byte[] buffer, int offset) {
        m_nocIdInstanceId = StringUtil.toInt(buffer, offset);
        m_hostId = StringUtil.toInt(buffer, offset + 4);
        m_hostIdAsLong = parseHostIdAsLong(m_nocIdInstanceId, m_hostId);
        m_strFormattedAddress = formatToStringAddress();
    }

    public void initialize(byte[] buffer) {
        initialize(buffer, 0);
    }

    /**
     * Gets the NOC Id associated with the Z7TransportAddress.
     */
    public int getNOCId() {
        return 0;
    }

    /**
     * Gets the NOC type of the Z7TransportAddress.
     */
    public byte getNOCType() {
        return (byte) 0;
    }

    /**
     * Gets the Host Id associated with the Z7TransportAddress.
     */
    public long getHostId() {
        return m_hostIdAsLong;
    }

    public int getHostIdLowBytes() {
        return m_hostId;
    }

    /**
     * Gets the host type of the Z7TransportAddress.
     */
    public byte getHostType() {
        return (byte) 0;
    }

    /**
     * Gets the Instance Id associated with the Z7TransportAddress.
     */
    public byte getInstanceId() {
        return (byte)(m_nocIdInstanceId & 0x000000ff);
    }

    /**
     * The combination of the noc id and the instance id
     */
    public int getNOCIdInstanceId()
    {
        return m_nocIdInstanceId;
    }

    /**
     * Returns a hash code value for the object.
     */
    public int hashCode() {
        // the instance id is left out from the hash calculation
        int hash = (m_nocIdInstanceId >> 8) + m_hostId;
        hash += ~(hash << 9);
        hash ^= (hash >> 14);
        hash += (hash << 4);
        hash ^= (hash >> 10);
        return hash;
    }

    /* not ported
    int32 Z7TransportAddress::compareFunction(const void *o1, const void *o2)
    */

    /**
     * Indicates if a pair of Z7TPAdress values have local addresses relative to each other, that is, they have the same nocId.
     * @param addr
     * @return boolean result of
    public boolean isLocal(Z7TransportAddress addr) {
    int nocId = m_nocIdInstanceId & 0xffffff00;
    int addrNocId = addr.m_nocIdInstanceId & 0xffffff00;
    return ( nocId == addrNocId );
    }

    private void writeObject(ObjectOutputStream out)
    throws IOException
    {
    out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
    in.defaultReadObject();
    m_strFormattedAddress = formatToStringAddress();
    }
     */

    /**
     * If the address minus the instance id is equal
     */
    public boolean equalsBase(Z7TransportAddress addr)
    {
        return ((m_nocIdInstanceId & 0xffffff00) == (addr.m_nocIdInstanceId & 0xffffff00))
                && (m_hostId == addr.m_hostId);
    }

    public String toString() {
        if(m_strFormattedAddress == null)
            m_strFormattedAddress = formatToStringAddress();

        return m_strFormattedAddress;
    }

    private String formatToStringAddress() {
        StringBuffer sb = new StringBuffer();
        sb.append(Integer.toHexString(getNOCId()));
        sb.append("-");
        sb.append(Long.toHexString(getHostId()));
        sb.append("-");
        String hex = Integer.toHexString((int) getInstanceId());
        sb.append(hex.length() > 2 ? hex.substring(hex.length() - 2) : hex);
        return sb.toString();
    }

    /**
     * Constructs a newly allocated Z7TransportAddress using the string.
     */
    public Z7TransportAddress(String address) {
        m_nocIdInstanceId = Z7TransportConstantsIF.Z7_TRANSPORT_INSTANCE_ID_DEFAULT_ANY;
        m_hostId = 0;
        if (address == null) {
            return;
        }

        Vector v = new Vector();
        String token = "-";
        while(address.indexOf(token)  != -1) {
            v.addElement(address.substring(0, address.indexOf(token)));
            address = address.substring(address.indexOf(token)+token.length());
        }
        v.addElement(address);

        if (v.size() != 3) {
            return;
        }
        String s = (String)v.elementAt(0);
        int nocId = Integer.parseInt(s, 16);

        s = (String)v.elementAt(1);
        long hostId = Long.parseLong(s, 16);

        s = (String)v.elementAt(2);
        int instanceId = Integer.parseInt(s, 16);

        m_nocIdInstanceId = ((int) ((0x00ffffff00000000l & hostId) >> 24)) | (0x000000ff & instanceId);
        m_hostId = (int) (0x00000000ffffffffl & hostId);
        m_hostIdAsLong = hostId;
    }

    protected long parseHostIdAsLong(int nocIdInstanceId, int hostId) {
        long longHostId = (((long)nocIdInstanceId & 0x00000000ffffff00l)) << 24;
        longHostId |= ((long)hostId & 0x00000000ffffffffl);
        return longHostId;
    }
}
