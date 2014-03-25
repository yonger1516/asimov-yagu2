package com.seven.asimov.it.base.constants;

public interface OCClientCertConstantsIF {
	
	public static final char[] DEFAULT_KS_PWD = "changeit".toCharArray();
	public static final char[] EMPTY_PWD = "".toCharArray();
	
	public static final String OC_ROOT_ALIAS = "ocroot";
	public static final String OC_ISSUER_ALIAS = "ocissuer";
	public static final String OC_REVOKED_ALIAS = "ocrevoked";
	public static final String OC_CA_ALIAS = "openchannel_ca";
	
	public static final String SELF_SIGNED_DN = "C=US,O=Self Signed,OU=Self Signed";
	
	public static final int RSA_KEY_SIZE = 2048;
}
