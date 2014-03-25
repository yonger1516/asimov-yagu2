package com.seven.asimov.it.base.https;

import javax.net.ssl.X509TrustManager;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * Implementation of the X509TrustManager which allows an application access to
 * certificate errors, and whether they should be accepted.
 * 
 * @author mweinberger
 * 
 */
public class FilteredX509TrustManager implements X509TrustManager {
	X509TrustManager mDefaultTrustManager;
	IX509TrustManagerFilter mFilter;

	public FilteredX509TrustManager(IX509TrustManagerFilter filter)
			throws NoSuchAlgorithmException, KeyStoreException {
		mDefaultTrustManager = CertUtils.getDefaultX509TrustManager();
		if (mDefaultTrustManager == null) {
			throw new NoSuchAlgorithmException(
					"no instance of X509TrustManager found");
		}
		mFilter = filter;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		try {
			mDefaultTrustManager.checkClientTrusted(chain, authType);
		} catch (CertificateException e) {
			if (mFilter != null && !mFilter.acceptClientCertificateException(e)) {
				throw e;
			}
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
			throws CertificateException {
		try {
			mDefaultTrustManager.checkServerTrusted(chain, authType);
		} catch (CertificateException e) {
			if (mFilter != null && !mFilter.acceptServerCertificateException(e)) {
				throw e;
			}
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
