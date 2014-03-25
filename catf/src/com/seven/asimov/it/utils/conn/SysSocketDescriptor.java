package com.seven.asimov.it.utils.conn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * This class used to hold collected socket information.
 * Class is used from native code.
 *
 */
public final class SysSocketDescriptor
{
    /**
     * TCP Status   
     *
     */
    public enum TcpStatus {
        ERROR_STATUS,
        TCP_ESTABLISHED,
        TCP_SYN_SENT,
        TCP_SYN_RECV,
        TCP_FIN_WAIT1,
        TCP_FIN_WAIT2,
        TCP_TIME_WAIT,
        TCP_CLOSE,
        TCP_CLOSE_WAIT,
        TCP_LISTEN,
        TCP_CLOSING;
        
        public static TcpStatus getStatus(int index) {
            if (index <0 || index > TcpStatus.values().length) {
                return null;
            }
            
            return TcpStatus.values()[index];
        }
    }
    
    public byte[] mSrcAddress;
    public byte[] mDstAddress;
	public int mSrcPort;
    public int mDstPort;
	public int mUserID;
	public TcpStatus mStatus;
	
	public SysSocketDescriptor(byte[] srcAddress, byte[] dstAddress, int srcPort, int dstPort, int userID) {
	    mSrcAddress = srcAddress;
        mDstAddress = dstAddress;
        mSrcPort = srcPort;
        mDstPort = dstPort;
        mUserID = userID;
	}
	
	public SysSocketDescriptor() {
	    this(null, null, -1, -1, -1);
	}
	
	public boolean isLeaked() {
	    if (TcpStatus.TCP_CLOSE_WAIT == mStatus) {
	        return true;
	    }
	    
	    return false;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(mDstAddress);
        result = prime * result + mDstPort;
        result = prime * result + Arrays.hashCode(mSrcAddress);
        result = prime * result + mSrcPort;
        result = prime * result + ((mStatus == null) ? 0 : mStatus.hashCode());
        result = prime * result + mUserID;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SysSocketDescriptor other = (SysSocketDescriptor) obj;
        if (!Arrays.equals(mDstAddress, other.mDstAddress))
            return false;
        if (mDstPort != other.mDstPort)
            return false;
        if (!Arrays.equals(mSrcAddress, other.mSrcAddress))
            return false;
        if (mSrcPort != other.mSrcPort)
            return false;
        if (mStatus != other.mStatus)
            return false;
        if (mUserID != other.mUserID)
            return false;
        return true;
    }

    @Override
    public String toString() {
        String srcAddr = "";
        String dstAddr = "";
        
        try {
            srcAddr = InetAddress.getByAddress(mSrcAddress).getHostAddress();
            srcAddr = InetAddress.getByAddress(mSrcAddress).getHostAddress();
        } catch (UnknownHostException e) {
        }
        
        return "SysSocketDescriptor [mSrcAddress=" + srcAddr + ", mDstAddress="
                + dstAddr + ", mSrcPort=" + mSrcPort + ", mDstPort=" + mDstPort + ", mUserID="
                + mUserID + ", mStatus=" + mStatus + "]";
    }
}
