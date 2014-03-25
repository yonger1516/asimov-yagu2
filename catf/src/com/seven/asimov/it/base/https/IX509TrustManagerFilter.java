package com.seven.asimov.it.base.https;

import java.security.cert.CertificateException;

/**
 * Interface class for providing filtering of Certificate exceptions.
 * 
 * @author mweinberger
 * 
 */
public abstract class IX509TrustManagerFilter {

	/**
	 * Called when a client certificate exception is encountered.
	 * 
	 * @param e
	 * @return Whether the exception is accepted.
	 */
	public abstract boolean acceptClientCertificateException(
			CertificateException e);

	/**
	 * Called when a server certificate exception is encountered.
	 * 
	 * @param e
	 * @return Whether the exception is accepted.
	 */
	public abstract boolean acceptServerCertificateException(
			CertificateException e);
}
