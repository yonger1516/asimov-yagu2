package com.seven.asimov.it.base.https;

import android.content.*;
import android.database.Cursor;
import android.util.Log;
import com.seven.asimov.it.base.constants.OCClientCertConstantsIF;
import com.seven.asimov.it.base.https.CertDBContent.OCCerts;
import com.seven.asimov.it.base.https.CertDBContent.Status;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.logging.Logger;

import static com.seven.asimov.it.base.constants.CertDBConstantsIF.*;
import static com.seven.asimov.it.base.constants.TFConstantsIF.OC_CHECK_ALL_CERTIFICATE_EXCEPTIONS;

public class CertDBUtility {
	
	private static final ContentProviderResult[] DEFAULT_RESULT = new ContentProviderResult[0];
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CertDBUtility.class.getSimpleName());
	
	private CertDBUtility() {}
	
	public static ContentValues getCertInsertCV(String hostName) {
		ContentValues cv = new ContentValues();
		cv.put(OCCerts.HOST_NAME, hostName);
		cv.put(OCCerts.STATUS, Status.NEW);
		cv.put(OCCerts.NOT_BEFORE, -1);
		cv.put(OCCerts.NOT_AFTER, -1);
		cv.put(OCCerts.LAST_ACCESS_TIME, System.currentTimeMillis());
		cv.put(OCCerts.ORIGIN_HASH, (String)null);
		cv.put(OCCerts.EXCEPTIONS, OC_CHECK_ALL_CERTIFICATE_EXCEPTIONS);

		cv.put(OCCerts.CERTIFICATE, (byte[])null);
		cv.put(OCCerts.PR_KEY, (byte[])null);

		cv.putNull(OCCerts.CIPHER_SUITE);
		cv.putNull(OCCerts.E_CACHE_KEY);

		return cv;
	}
	
	public static ContentValues getCertInvalidateCV(int invalidateCount) {
		ContentValues cv = new ContentValues();
		if (invalidateCount >= MAX_INVALIDATE_COUNT) {
			cv.put(OCCerts.STATUS, Status.INVALID);
			cv.put(OCCerts.INVALIDATE_COUNT, MAX_INVALIDATE_COUNT);
		} else {
			cv.put(OCCerts.STATUS, Status.NEW);
			cv.put(OCCerts.NOT_BEFORE, -1);
			cv.put(OCCerts.NOT_AFTER, -1);
			cv.put(OCCerts.LAST_ACCESS_TIME, -1);
			cv.put(OCCerts.ORIGIN_HASH, (String)null);
			cv.put(OCCerts.EXCEPTIONS, OC_CHECK_ALL_CERTIFICATE_EXCEPTIONS);
			cv.put(OCCerts.INVALIDATE_COUNT, invalidateCount);
	
			cv.put(OCCerts.CERTIFICATE, (byte[])null);
			cv.putNull(OCCerts.CERTIFICATE_ID);
			cv.put(OCCerts.PR_KEY, (byte[])null);
			
			cv.putNull(OCCerts.CIPHER_SUITE);
			cv.putNull(OCCerts.E_CACHE_KEY);
		}
		return cv;
	}
	
	public static ContentValues getCertCV(String hostName, int status, 
			long notBefore, long notAfter, String hash, 
			int exceptions, byte[] certBlob, byte[] prkeyBlob) {
		
		ContentValues cv = new ContentValues();
		cv.put(OCCerts.HOST_NAME, hostName);
		cv.put(OCCerts.STATUS, status);
		cv.put(OCCerts.NOT_BEFORE, notBefore);
		cv.put(OCCerts.NOT_AFTER, notAfter);
		cv.put(OCCerts.LAST_ACCESS_TIME, System.currentTimeMillis());
		cv.put(OCCerts.ORIGIN_HASH, hash);
		cv.put(OCCerts.EXCEPTIONS, exceptions);
		cv.put(OCCerts.ERROR_COUNT, 0);

		cv.put(OCCerts.CERTIFICATE, certBlob);
		cv.put(OCCerts.PR_KEY, prkeyBlob);
		
		cv.putNull(OCCerts.CIPHER_SUITE);
		cv.putNull(OCCerts.E_CACHE_KEY);
		
		return cv;
	}

	
	public static ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> opsBatch, String authority, Context androidCtx, Logger log) {
		if(opsBatch == null || opsBatch.size() <= 0) {
			return DEFAULT_RESULT;
		}
		
		final ContentProviderClient providerClient = androidCtx.getContentResolver().acquireContentProviderClient(authority);
		
		if (providerClient != null) {
			try {
				ContentProviderResult[] results = providerClient.applyBatch(opsBatch);
				return results;
			} catch(Exception e) {
				logger.error("Error in applyBatch", e);
			} finally {
				providerClient.release();
			}
		}
		
		return DEFAULT_RESULT;
	}
	
	
	public static ContentProviderOperation getUpdateToPendingOp(String hostName) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.STATUS, Status.PENDING)
				.withValue(OCCerts.NOT_BEFORE, -1)
				.withValue(OCCerts.NOT_AFTER, -1)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getUpdateToNewOp(String hostName) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.STATUS, Status.NEW)
				.withValue(OCCerts.NOT_BEFORE, -1)
				.withValue(OCCerts.NOT_AFTER, -1)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getUpdateToNewOpWithError(String hostName, int errCount) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.STATUS, Status.NEW)
				.withValue(OCCerts.ERROR_COUNT, errCount)
				.withValue(OCCerts.NOT_BEFORE, -1)
				.withValue(OCCerts.NOT_AFTER, -1)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getUpdateToPredownloadOp(String hostName) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.STATUS, Status.PREDOWNLOAD)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getUpdateToPredownloadPendingOp(String hostName) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.STATUS, Status.PREDOWNLOAD_PENDING)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getUpdateToPredownloadPendingOpWithError(String hostName, int errCount) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.STATUS, Status.PREDOWNLOAD_PENDING)
				.withValue(OCCerts.ERROR_COUNT, errCount)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getUpdateCertOp(String hostName, ContentValues cv) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValues(cv)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getInsertNewOp(String hostName) {
		ContentValues cv = CertDBUtility.getCertInsertCV(hostName);
		return 
				ContentProviderOperation.newInsert(OCCerts.CONTENT_URI)
				.withValues(cv)
				.build();
	}
	
	public static ContentProviderOperation getInvalidateAllOp() {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValues(getCertInvalidateCV(0))
				.build();
	}
	
	public static ContentProviderOperation getResetPendingOp() {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValues(getCertInvalidateCV(0))
				.withSelection(SEL_PENDING, null)
				.build();
	}
	
	public static ContentProviderOperation getResetPredownloadsOp() {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.STATUS, Status.AVAILABLE)
				.withSelection(SEL_PREDOWNLOAD_STATE, null)
				.build();
	}
	
	public static ContentProviderOperation getInvalidateOp(String hostName, int invalidateCount) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValues(getCertInvalidateCV(invalidateCount))
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
    public static ContentProviderOperation getInvalidateOp(String hostName, int invalidateCount, 
            int refreshType, String hashCode) {
        
        String refreshHints = "";
        if (refreshType == REFRESH_TYPE_MISMATCH || refreshType == REFRESH_TYPE_APP_HANDSHAKE_REJECTION ) {
            if (hashCode == null ) {
                refreshHints = refreshType + ";";
            } else {
                refreshHints = REFRESH_TYPE_MISMATCH + ";" + hashCode;
            }
        }

        ContentValues cv = getCertInvalidateCV(invalidateCount);
        cv.put(OCCerts.REFRESH_HINTS, refreshHints);
        
        return ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
                .withValues(cv)
                .withSelection(SEL_HOST_NAME, new String[]{hostName})
                .build();
    }
    
    public static ContentProviderOperation getDeleteOp(String hostName) {
		return 
				ContentProviderOperation.newDelete(OCCerts.CONTENT_URI)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getInsertOp(ContentValues cv) {
		return 
				ContentProviderOperation.newInsert(OCCerts.CONTENT_URI)
				.withValues(cv)
				.build();
	}
	
	public static ContentProviderOperation getSetErrorCountOp(String hostName, int errCount) {
		return 
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.ERROR_COUNT, errCount)
				.withSelection(SEL_HOST_NAME, new String[]{hostName})
				.build();
	}
	
	public static ContentProviderOperation getSetCipherSuiteOp(String hostname, String cipherSuite, byte[] eCacheKey) {
		return
				ContentProviderOperation.newUpdate(OCCerts.CONTENT_URI)
				.withValue(OCCerts.CIPHER_SUITE, cipherSuite)
				.withValue(OCCerts.E_CACHE_KEY, eCacheKey)
				.withSelection(SEL_HOST_NAME, new String[]{hostname})
				.build();
	}
	
	protected static boolean hostNameSaved(String hostName, Context ctx, Logger log) {
		return (getHostNameStatus(hostName, ctx, log) > 0);
	}
	
	
	protected static int getHostNameStatus(String hostName, Context ctx, Logger log) {
		Cursor c = null;
		try {
			c = ctx.getContentResolver().query(OCCerts.CONTENT_URI, PRJ_HOST_NAME, SEL_HOST_NAME, new String[] {hostName}, null);
			if(c != null && c.moveToFirst()) {
				return c.getInt(c.getColumnIndex(OCCerts.STATUS));
			}
		} catch(Exception e) {
			logger.error("Error in getHostNameStatus ", e);
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return -1;
	}
	
	protected static String getCACertHash(Context ctx) {
		Cursor c = null;
		try {
			c = ctx.getContentResolver().query(OCCerts.CONTENT_URI, PRJ_ORIGIN_HASH, SEL_HOST_NAME, new String[] {OCClientCertConstantsIF.OC_CA_ALIAS}, null);
			if(c != null && c.moveToFirst()) {
				return c.getString(c.getColumnIndex(OCCerts.ORIGIN_HASH));
			}
		} catch(Exception e) {
			
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return null;
	}

	/**
	 * Obtain the error count for the given host name.
	 * @param ctx
	 * @param hostname
	 * @return
	 */
	public static int getCertificateErrorCount(Context ctx, String hostname) {
		Cursor c = null;
		int count = 0;
		try {
			c = ctx.getContentResolver().query(OCCerts.CONTENT_URI, PRJ_ERROR_COUNT, SEL_HOST_NAME, new String[] {hostname}, null);
			if(c != null && c.moveToFirst()) {
				count = c.getInt(c.getColumnIndex(OCCerts.ERROR_COUNT));
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}

		return count;
	}

	public static String getCipherSuite(Context ctx, String hostname) {
		Cursor c = null;
		String cipherSuite = null;
		try {
			c = ctx.getContentResolver().query(OCCerts.CONTENT_URI, PRJ_CIPHER_SUITE, SEL_HOST_NAME, new String[] {hostname}, null);
			if(c != null && c.moveToFirst()) {
				cipherSuite = c.getString(c.getColumnIndex(OCCerts.CIPHER_SUITE));
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return cipherSuite;
	}
	
	public static byte[] getCertificate(Context ctx, String hostname) {
		Cursor c = null;
		byte[] certificate = null;
		try {
			c = ctx.getContentResolver().query(OCCerts.CONTENT_URI, PRJ_CERT_N_KEY, SEL_HOST_NAME, new String[] {hostname}, null);
			if(c != null && c.moveToFirst()) {
				certificate = c.getBlob(c.getColumnIndex(OCCerts.CERTIFICATE));
			}
		} finally {
			if(c != null) {
				c.close();
			}
		}
		return certificate;
	}
}
