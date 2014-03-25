package com.seven.asimov.it.base.https;

import com.seven.asimov.it.base.StringProperties;

import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class CertificateWrapper {

    X509Certificate mCertificate;
    
    public CertificateWrapper(Certificate certificate) {
        mCertificate = (X509Certificate) certificate;
    }
    
    public int getPublicKeyLength() {
        return getPublicKeyLength(8);
    }
    
    /**
     * Returns public key length, counting leading zeroes.
     * If public key has length e.g 1024 bits, but first bits are zeroes,
     * getPublicKey() will return e.g. 1022 bits.
     * @param roundBits bits to round, example: 8 - rounds to one byte 
     * (leading 11111 will treated as 00011111). 0 - no rounding
     * @return
     */
    public int getPublicKeyLength(int roundBits) {
        int len = getPublicKeyLengthRaw();
        if (len == -1) return -1;
        if (roundBits == 0) return len;
        int rem = len % roundBits;
        int add = rem == 0 ? 0 : roundBits - rem;
        return len + add;
    }
    
    public int getPublicKeyLengthRaw() {
        if (mCertificate.getPublicKey() instanceof RSAPublicKey) {
            RSAPublicKey key = (RSAPublicKey) mCertificate.getPublicKey();
            return key.getModulus().bitLength();
        } else if (mCertificate.getPublicKey() instanceof DSAPublicKey){
            DSAPublicKey key = (DSAPublicKey) mCertificate.getPublicKey();
            return key.getY().bitLength();
        }
        return -1;
    }
    
    public String getSigAlgName() {
        return mCertificate.getSigAlgName();
    }
    
    public X509Certificate getCertificate() {
        return mCertificate;
    }
    
    public int getVersion() {
        return mCertificate.getVersion();
    }
    
    public String getSubject() {
        return mCertificate.getSubjectX500Principal().getName();
    }
    
    public String getIssuer() {
        return mCertificate.getIssuerX500Principal().getName();
    }
    
    public String getSubjectCn() {
        return StringProperties.getProperty(getSubject(), CertUtils.CRT_SUBJECT_CN, ",") ;
    }
    
    public boolean[] getKeyUsage() {
        return mCertificate.getKeyUsage();
    }
    
    public int getBasicConstraints() {
        return mCertificate.getBasicConstraints();
    }
    
    public byte[] getBasicConstraintsDer() {
        return mCertificate.getExtensionValue(CertUtils.CRT_BASIC_CONSTRAINTS_OID);
    }
    
    public byte[] getAuthorityKeyIdDer() {
        return mCertificate.getExtensionValue(CertUtils.CRT_AUTHORITY_KEY_ID_OID);
    }
    
    public byte[] getSubjectKeyIdDer() {
        return mCertificate.getExtensionValue(CertUtils.CRT_SUBJECT_KEY_ID_OID);
    }
    
    public String getKeyUsageAsString() {
        if (mCertificate.getKeyUsage() == null) return null;
        StringBuilder sb = new StringBuilder();
        boolean addComma  = false;
        for (int i = 0; i < mCertificate.getKeyUsage().length; i++) {
            if (mCertificate.getKeyUsage()[i]) {
                if (!addComma) {
                    addComma = true;
                } else {
                    sb.append(", ");
                }
                sb.append(CertUtils.CRT_KEY_USAGE_NAMES[i]);
            }
        }
        return sb.toString();
    }
    
    public boolean isKeyUsageCritical() {
        return mCertificate.getCriticalExtensionOIDs().contains(CertUtils.CRT_KEY_USAGE_OID);
    }
    
    public BigInteger getSerialNumber() {
        return mCertificate.getSerialNumber();
    }
    
    public Collection<List<?>> getSubjectAlternativeNames() throws Exception {
        return mCertificate.getSubjectAlternativeNames();
    }
    
    public boolean isSubjectAlternativeNamesCritical() {
        return mCertificate.getCriticalExtensionOIDs().contains(CertUtils.CRT_SUBJECT_ALT_NAME);
    }
    
    public Date getNotBefore() {
        return mCertificate.getNotBefore();
    }
    
    public Date getNotAfter() {
        return mCertificate.getNotAfter();
    }

    public String getSubjectAlternativeNamesAsString() throws Exception {
        if (mCertificate.getSubjectAlternativeNames() == null) return "";
        String[] strings = new String[mCertificate.getSubjectAlternativeNames().size()];
        int i=0;
        for (List<?> list : mCertificate.getSubjectAlternativeNames()) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Object object : list) {
                if (object instanceof Integer) {
                    int type = (Integer) object;
                    if (type >= 0 && type < CertUtils.CRT_GENERAL_NAME_TYPES.length) {
                        sb.append(CertUtils.CRT_GENERAL_NAME_TYPES[type]);
                    } else {
                        sb.append("Unknown");
                    }
                    sb.append(": ");
                } else if (object instanceof String) {
                    sb.append(object);
                } else {
                    sb.append("unknown object");
                }
            }
            sb.append("}");
            strings[i++] = sb.toString();
        }
        return Arrays.toString(strings);
    }
    
}
