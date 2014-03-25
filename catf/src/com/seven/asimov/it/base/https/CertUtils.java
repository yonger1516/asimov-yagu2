package com.seven.asimov.it.base.https;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class CertUtils {
    
    public static final String CERT_TYPE_X509 = "X.509";
    public static final String KP_ALG_RSA = "RSA";
    
    public static final String[] CRT_KEY_USAGE_NAMES = {   
        "digitalSignature",
        "nonRepudiation",
        "keyEncipherment",
        "dataEncipherment",
        "keyAgreement",
        "keyCertSign",
        "cRLSign",
        "encipherOnly",
        "decipherOnly" };
    
    // org.bouncycastle.asn1.x509.GeneralName
    public static final String[] CRT_GENERAL_NAME_TYPES = 
    {
        "otherName",
        "rfc822Name",
        "dNSName",
        "x400Address",
        "directoryName",
        "ediPartyName",
        "uniformResourceIdentifier",
        "iPAddress",
        "registeredID"
    };
    
    public static final String CRT_KEY_USAGE_OID = "2.5.29.15"; // keyUsage, http://www.oid-info.com/cgi-bin/display?oid=2.5.29.15&action=display
    public static final String CRT_AUTHORITY_KEY_ID_OID = "2.5.29.35";
    public static final String CRT_SUBJECT_KEY_ID_OID = "2.5.29.14";
    public static final String CRT_SUBJECT_ALT_NAME = "2.5.29.17"; 
    public static final String CRT_BASIC_CONSTRAINTS_OID = "2.5.29.19";
    
    
    public static final String CRT_SUBJECT_CN = "CN";
    
    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    
	private static X509TrustManager sTrustManager = null;
	private static X509Certificate[] sAcceptedIssuers;
	
    /**
     * Given a CertificateException, dig through causes to find the bottom-most
     * CertificateException cause by iteratively calling {@code Throwable.getCause()}.
     * Will always return a CertificateException, even when that exception has a different
     * cause within it.
     * 
     * @param e CertificateException to dig through
     * @return bottom-most CertificateException in chain of exception causes
     */
    public static CertificateException getCertificateExceptionCause(CertificateException e) {
        if(e == null)
            throw new IllegalArgumentException("exception is null");
        Throwable t = e.getCause();
        int nMaxIterations = 10;
        while(nMaxIterations-- > 0) {
            if(t == null || t == e)
                return e;
            else if(t instanceof CertificateException)
                e = (CertificateException) t;
            t = t.getCause();
        }
        return null;
    }
    
    /**
     * Log a formatted presentation of a CertificateException showing nested exceptions.
     */
    public static void logCertificateException(String logPrefix, CertificateException e) {
        StringBuilder sb = new StringBuilder();
        sb.append(logPrefix).append(": ").append(e.toString()).append('\n');
        for(Throwable t = e.getCause(); t != null; t = t.getCause())
            sb.append("  cause: ").append(t.toString()).append('\n');
        System.out.println(sb.toString());
    }
    
    public static X509Certificate deserializeCertificate(byte[] certBlob, String providerType) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(certBlob);
			return deserializeCertificate(is, providerType);
		} catch(Exception e) {
		    System.err.println("Error in deserializeCertificate ");
		    e.printStackTrace();
		}
		return null;
	}
	
    public static X509Certificate deserializeCertificate(InputStream is, String providerType) {
		
    	try {
			CertificateFactory cf = CertificateFactory.getInstance(
					CERT_TYPE_X509, 
					providerType);
			
			X509Certificate x509cert = (X509Certificate) cf.generateCertificate(is);
			return x509cert;
		} catch (Exception e) {
		    System.err.println("Error in deserializeCertificate ");
		    e.printStackTrace();
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}
		return null;
    }
    
    public static PrivateKey deserializePrivateKey(byte[] privKeyBlob, String providerType) {
    	
    	try {
    		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyBlob);
    		KeyFactory keyFac = KeyFactory.getInstance(KP_ALG_RSA, providerType);
    		PrivateKey privKey = keyFac.generatePrivate(keySpec);
    		return privKey;
    	} catch(Exception e) {
    	    System.err.println("Error in deserializePrivateKey");
    	    e.printStackTrace();
    	}
    	return null;
    }
    
    
    public static X509Certificate isAcceptedIssuer(X509Certificate cert, boolean cacheCompare) {
    	X509Certificate[] issuers = getAcceptedIssuers(cacheCompare);
    	if(issuers != null) {
    		String searchName = cert.getSubjectDN().getName();
    		for(X509Certificate issuer : issuers) {
    			String issuerName = issuer.getSubjectDN().getName();
    			if(searchName.equals(issuerName) && issuer.equals(cert)) {
    				return issuer;
    			}
    		}
    	}
    	return null;
    }

	public static X509TrustManager getDefaultX509TrustManager() {
		try {
			
			if(sTrustManager != null) {
				return sTrustManager;
			}
    		String defAlg = TrustManagerFactory.getDefaultAlgorithm();
    		TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(defAlg);
    		tmFactory.init((KeyStore)null);
    		TrustManager tmList[] = tmFactory.getTrustManagers();
    		for (int i = 0; i < tmList.length; i++) {
    			if (tmList[i] instanceof X509TrustManager) {
    				sTrustManager = (X509TrustManager) tmList[i];
    				return sTrustManager;
    			}
    		}
		} catch(Exception e) {
		    System.err.println("Error in hasTrustCertificate ");
		    e.printStackTrace();
    	}
		return null;
	}
	
	
	public static X509Certificate[] getAcceptedIssuers(boolean cacheCompare) {
		if(sAcceptedIssuers == null || !cacheCompare) {
			X509TrustManager x509Mgr = getDefaultX509TrustManager();
			if(x509Mgr != null) {
				sAcceptedIssuers = x509Mgr.getAcceptedIssuers();
			}
		}
		return sAcceptedIssuers;
	}
	
	public static boolean validateChain(X509Certificate[] chain, String authType) {
		
		try {
			X509TrustManager x509Mgr = getDefaultX509TrustManager();
			if(x509Mgr != null) {
				x509Mgr.checkServerTrusted(chain, authType);
				return true;
			}
			
		} catch (CertificateException e) {
		    System.err.println("Error in validateChain");
		    e.printStackTrace();
		}
		return false;
	}
	
	
    public static void dumpAliases(KeyStore ks) {
    	try {
    		Enumeration<String> aList = ks.aliases();
    		StringBuffer sbuf = new StringBuffer();
    		sbuf.append("Alias List - [ ");
    		while(aList.hasMoreElements()) {
    			sbuf.append(aList.nextElement());
    			sbuf.append(',');
    		}
    		sbuf.setLength(sbuf.length() - 1);
    		sbuf.append(" ]");
    		System.out.println(sbuf.toString());
    	} catch(Exception e) {
    	    System.err.println("Error in dumpAliases");
    	    e.printStackTrace();
    	}
    }
    
    public static CertificateInfo[] parseCertificateInfo(String jInfo) throws Exception {
        return parseCertificateInfo(new JSONObject(jInfo));
    }
    
    public static CertificateInfo[] parseCertificateInfo(JSONObject jInfo) throws Exception {
        List<CertificateInfo> list = new ArrayList<CertificateInfo>();
        
        JSONArray jArr = jInfo.getJSONArray("certificate_chain");
        for (int i = 0; i < jArr.length(); i++) {
            CertificateInfo ci = new CertificateInfo();
            list.add(ci);
            JSONObject jCrt = (JSONObject) jArr.get(i);
            String serialHex = jCrt.getString("serial_number");
            BigInteger serial = new BigInteger(serialHex, 16);
            ci.serialNumber = serial;
            ci.subject = jCrt.getString("subject");
            ci.issuer = jCrt.getString("issuer");
            ci.version = jCrt.getInt("version");
            
            JSONObject jSig = (JSONObject) jCrt.get("signature");
            ci.sigAlgName = jSig.getString("algorithm");
            ci.signature = hexStringToBytes(jSig.getString("value"));
            
            try {
                JSONObject jKey = (JSONObject) jCrt.get("public_key");
                ci.publicKeyLength = jKey.getInt("length");
            } catch (Exception e) {
                ci.publicKeyLength = -1;
            }
            
            if (jCrt.has("extensions")) {
                JSONObject jExt = (JSONObject) jCrt.get("extensions");
                if (jExt.has("key_usage")) {
                    JSONObject jKU = (JSONObject) jExt.get("key_usage");
                    ci.keyUsage = toBooleanArray(jKU.getString("value"));
                    ci.keyUsageCritical = jKU.getBoolean("critical");
                }
            }
            
        }
        
        return list.toArray(new CertificateInfo[0]);
    }
    
    public static String bytesToHexString(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(HEX_CHARS[(b >> 4) & 0x0F]);
            sb.append(HEX_CHARS[b & 0x0F]);
        }
        return sb.toString();
    }
    
    public static byte[] hexStringToBytes(String hex) throws Exception {
        if (hex.length() % 2 != 0) throw new Exception("Cannot parse string: odd count of characters");
        char[] chars = hex.toLowerCase().toCharArray();
        byte[] arr = new byte[chars.length / 2];
        int a, b =0;
        for (int i=0; i<chars.length; i+=2) {
            a = Character.digit(chars[i], 16);
            b = Character.digit(chars[i+1], 16);
            arr[i/2] = (byte) (a<<4 + b);
        }
        return arr;
    }
    
    public static  boolean[] toBooleanArray (String bits) {
        boolean[] arr = new boolean[bits.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (bits.charAt(i) == '1');
        }
        return arr;
    }
    
}
