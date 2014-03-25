package com.seven.asimov.it.base.https;

import java.math.BigInteger;

public class CertificateInfo {

    public BigInteger serialNumber;
    public String issuer;
    public String subject;
    public int publicKeyLength;
    public String sigAlgName;
    public int version;
    public byte[] signature;
    public boolean[] keyUsage;
    public boolean keyUsageCritical;

}
