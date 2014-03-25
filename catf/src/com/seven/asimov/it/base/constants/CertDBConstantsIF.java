package com.seven.asimov.it.base.constants;

import com.seven.asimov.it.base.https.CertDBContent.OCCerts;
import com.seven.asimov.it.base.https.CertDBContent.Status;

import static com.seven.asimov.it.base.constants.TFConstantsIF.*;

public interface CertDBConstantsIF {
	public static final int MAX_INVALIDATE_COUNT = 2;
	public static final int MAX_ERROR_COUNT = 2;

	public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;
    
    public static final String LIMIT = "limit";

    // Different reasons for requesting a certificate refresh.
    public static final int REFRESH_TYPE_DEFAULT = 0;
    public static final int REFRESH_TYPE_MISMATCH = 1;
    public static final int REFRESH_TYPE_APP_HANDSHAKE_REJECTION = 2;

	public static final String[] PRJ_HAS_CERT =  new String[] {OCCerts.STATUS};
	public static final String[] PRJ_ORIGIN_HASH =  new String[] {OCCerts.ORIGIN_HASH};
	public static final String[] PRJ_EXCEPTIONS =  new String[] {OCCerts.EXCEPTIONS};
	public static final String[] PRJ_INVALIDATE_COUNT =  new String[] {OCCerts.INVALIDATE_COUNT};
	public static final String[] PRJ_ERROR_COUNT =  new String[] {OCCerts.ERROR_COUNT};
	public static final String[] PRJ_CERT_N_KEY = new String[] {OCCerts.CERTIFICATE, OCCerts.PR_KEY, OCCerts.EXCEPTIONS};
	public static final String[] PRJ_HOST_NAME = new String[] {OCCerts.HOST_NAME, OCCerts.STATUS};
	public static final String[] PRJ_SAVED_TIME = new String[] {OCCerts.SAVED_TIME};
	public static final String[] PRJ_NOT_AFTER = new String[] {OCCerts.HOST_NAME, OCCerts.NOT_AFTER};
	public static final String[] PRJ_NOT_BEFORE = new String[] {OCCerts.HOST_NAME, OCCerts.NOT_BEFORE};
	public static final String[] PRJ_CERT_COUNT = new String[] {"COUNT(*) AS count"};
	public static final String[] PRJ_CIPHER_SUITE = new String[] {OCCerts.CIPHER_SUITE};
	public static final String[] PRJ_E_CACHE_KEY = new String[] {OCCerts.CERTIFICATE, OCCerts.PR_KEY, OCCerts.CIPHER_SUITE, OCCerts.E_CACHE_KEY};
	
	public static final String SEL_NOT_CA = "(" + OCCerts.HOST_NAME + " NOT LIKE '"+ OCClientCertConstantsIF.OC_CA_ALIAS +"')";
	public static final String SEL_USABLE = " (" + OCCerts.STATUS + " >= " + Status.AVAILABLE + " AND " + OCCerts.STATUS + " < " + Status.INVALID + " ) ";
	public static final String SEL_HOST_NAME = OCCerts.HOST_NAME + " = ?";
	public static final String SEL_AVAILABLE = OCCerts.STATUS + " >= " + Status.AVAILABLE;
    public static final String SEL_AVAILABLE_OR_INVALID = OCCerts.STATUS + " >= " + Status.AVAILABLE;
	public static final String SEL_EXACT_AVAILABLE = OCCerts.STATUS + " = " + Status.AVAILABLE;
	public static final String SEL_NEW = OCCerts.STATUS + " = " + Status.NEW;
	public static final String SEL_PENDING = OCCerts.STATUS + " = " + Status.PENDING;
	public static final String SEL_PREDOWNLOAD = OCCerts.STATUS + " = " + Status.PREDOWNLOAD;
	public static final String SEL_PREDOWNLOAD_PENDING = OCCerts.STATUS + " = " + Status.PREDOWNLOAD_PENDING;
	public static final String SEL_PREDOWNLOAD_STATE = OCCerts.STATUS + " >= " + Status.PREDOWNLOAD;
	
    public static final String SEL_AVAILABLE_OR_INVALID_HOST = SEL_HOST_NAME + " AND " + SEL_AVAILABLE_OR_INVALID;

	public static final String SEL_AVAILABLE_HOST = SEL_HOST_NAME + " AND " + SEL_AVAILABLE;
	public static final String SEL_DOWNLOAD = SEL_NOT_CA + " AND ("+ SEL_NEW + " OR " + SEL_PREDOWNLOAD + ")";
	
	public static final String SEL_HAS_NO_EXCEPTIONS = OCCerts.EXCEPTIONS + " = " + OC_CHECK_ALL_CERTIFICATE_EXCEPTIONS;
	public static final String SEL_HAS_EXPIRED_EXCEPTION = OCCerts.EXCEPTIONS + " = " + OC_IGNORE_CERTIFICATE_EXPIRED_EXCEPTION;
	
	public static final String SEL_HAS_NOT_YET_VALID_EXCEPTION = "(" 
			+ OCCerts.EXCEPTIONS + " = " + OC_IGNORE_CERTIFICATE_NOT_YET_VALID_EXCEPTION
			+ " OR "
			+ OCCerts.EXCEPTIONS + " = " 
				+ (OC_IGNORE_CERTIFICATE_AUTHORITY_EXCEPTIONS | OC_IGNORE_CERTIFICATE_NOT_YET_VALID_EXCEPTION)
			+ ")";
	
	public static final String SEL_HAS_DATE_EXCEPTIONS = SEL_HAS_EXPIRED_EXCEPTION + " OR " + SEL_HAS_NOT_YET_VALID_EXCEPTION;
	public static final String SEL_HAS_NO_DATE_EXCEPTIONS = "("
			+ OCCerts.EXCEPTIONS + " = " + OC_CHECK_ALL_CERTIFICATE_EXCEPTIONS
			+ " OR "
			+ OCCerts.EXCEPTIONS + " = " + OC_IGNORE_CERTIFICATE_AUTHORITY_EXCEPTIONS
			+ ")";
	
	
	
	// CERTS MONITOR INVALIDATION 
	public static final String INVALID_NOT_YET_VALID = "(" + SEL_HAS_NOT_YET_VALID_EXCEPTION + " AND " + OCCerts.NOT_BEFORE + " <= ? )";
	public static final String INVALID_EXPIRED = "(" + SEL_HAS_EXPIRED_EXCEPTION + " AND " + OCCerts.NOT_AFTER + " >= ?" + ")";
	public static final String INVALID_SHELF_LIFE = "(" 
			+ SEL_NOT_CA 
			+ " AND " 
			+ "(" + OCCerts.SAVED_TIME + " > ? OR " + OCCerts.SAVED_TIME +" <= ? )" 
			+ ")";
	
	public static final String INVALID_GOOD_CERT = "(" 
			+ SEL_HAS_NO_DATE_EXCEPTIONS 
			+ " AND "
			+ "(" + OCCerts.NOT_AFTER + " <= ?  OR "+ OCCerts.NOT_BEFORE + " >= ? )"
			+ ")";
	
	
	public static final String SEL_INVALID = 
			SEL_AVAILABLE 
			+ " AND ("
				+ INVALID_GOOD_CERT
				+ " OR "
				+ INVALID_NOT_YET_VALID 
				+ " OR "
				+ INVALID_EXPIRED
				+ " OR "
				+ INVALID_SHELF_LIFE
			+ " )";

	public static final String SEL_INVALID_ALARM_GOOD_CERT = SEL_AVAILABLE + " AND " + SEL_HAS_NO_DATE_EXCEPTIONS;
	public static final String SEL_INVALID_ALARM_NOT_YET_VALID_CERT = SEL_AVAILABLE + " AND " + SEL_HAS_NOT_YET_VALID_EXCEPTION;
	
	
	// CERTS MOITOR PREDOWNLOAD
	public static final String SOON_TO_BE_INVALID_GOOD_CERT = "( (" + OCCerts.NOT_AFTER + " BETWEEN ? AND ? ) AND " + SEL_HAS_NO_DATE_EXCEPTIONS + ")";
	public static final String SOON_TO_BE_INVALID_NOT_YET_VALID = "( (" + OCCerts.NOT_BEFORE + " BETWEEN ? AND ? ) AND " + SEL_HAS_NOT_YET_VALID_EXCEPTION + " )";
	public static final String SOON_TO_BE_INVALID_SHELF_LIFE = "(" + OCCerts.SAVED_TIME + " BETWEEN ? AND ? )";
	

	public static final String SEL_SOON_TO_BE_INVALID = 
			SEL_NOT_CA + " AND " + SEL_USABLE 
			+ " AND ( "
				+ SOON_TO_BE_INVALID_GOOD_CERT
				+ " OR "
				+ SOON_TO_BE_INVALID_NOT_YET_VALID
				+ " OR "
				+ SOON_TO_BE_INVALID_SHELF_LIFE
			+ " )";
	
	public static final String SEL_PREDOWNLOAD_ALARM_GOOD_CERT = 
			SEL_NOT_CA + " AND "
			+ SEL_EXACT_AVAILABLE + " AND " 
			+ SEL_HAS_NO_DATE_EXCEPTIONS + " AND " 
			+ OCCerts.NOT_AFTER + " >= ? ";
	
	public static final String SEL_PREDOWNLOAD_ALARM_NOT_YET_VALID_CERT =
			SEL_NOT_CA + " AND "
			+ SEL_EXACT_AVAILABLE + " AND " 
			+ SEL_HAS_NOT_YET_VALID_EXCEPTION + " AND " 
			+ OCCerts.NOT_BEFORE + " >= ?";

	public static final String SEL_PREDOWNLOAD_ALARM_EXPIRED_SHELF_LIFE =
			SEL_NOT_CA + " AND "
			+ SEL_EXACT_AVAILABLE + " AND "
			+ OCCerts.SAVED_TIME + " >= ?";
	
	// SORT ORDER
	public static final String SORT_NOT_AFTER_ASC = OCCerts.NOT_AFTER + " ASC";
	public static final String SORT_NOT_BEFORE_ASC = OCCerts.NOT_BEFORE + " ASC";
	public static final String SORT_LAST_ACCESS_ASC = OCCerts.LAST_ACCESS_TIME + " ASC";
	public static final String SORT_SAVED_TIME_ASC = OCCerts.SAVED_TIME + " ASC";
}
