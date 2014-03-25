package com.seven.asimov.it.base.https;

import android.content.*;
import android.database.Cursor;
import android.util.Log;
import com.seven.asimov.it.base.constants.CertDBConstantsIF;
import com.seven.asimov.it.base.https.CertDBContent.OCCerts;
import com.seven.asimov.it.base.https.CertDBContent.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.seven.asimov.it.base.constants.CertDBConstantsIF.*;

/**
 * Utility functions for manipulating the asimov certificate store.
 * @author mweinberger
 *
 */
public class CertificateStore {

    private static final Logger logger = LoggerFactory.getLogger(CertificateStore.class.getSimpleName());

    /**
     * Determines whether a certificate exists for the specified hostname.
     * @param androidContext
     * @param hostName
     * @return
     */
   public static boolean hasCertificate(Context androidContext, String hostName) {
        Cursor c = null;
        try {
            c = androidContext.getContentResolver().query(OCCerts.CONTENT_URI, PRJ_HAS_CERT, SEL_HOST_NAME, new String[] {hostName}, null);
            if(c != null && c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(CertDBContent.OCCerts.STATUS));
                return (status == Status.AVAILABLE); 
            }
        } catch(Exception e) {
            logger.error("certificate store query failed: ", e);
        } finally {
            if(c != null) {
                c.close();
            }
        }

        return false;
    }
   
    /**
     * Returns certificate exceptions for the specified hostname.
     * 
     * @param androidContext
     * @param hostName
     * @return
     */
    public static int getCertificateExceptions(Context androidContext, String hostName) {
        Cursor c = null;
        try {
            c = androidContext.getContentResolver().query(OCCerts.CONTENT_URI, CertDBConstantsIF.PRJ_EXCEPTIONS,
                    SEL_HOST_NAME, new String[] { hostName }, null);
            if (c != null && c.moveToFirst()) {
                int exc = c.getInt(c.getColumnIndex(OCCerts.EXCEPTIONS));
                return exc;
            }
        } catch (Exception e) {
            logger.error("certificate store query failed: ", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return -1;
    }

	/**
	 * Removes the certificate for the given hostname.
	 * @param androidContext
	 * @param hostName
	 * @return
	 */
	public static boolean invalidateCertificate(Context androidContext, String hostName) {
		int count = 0;
		try {
			ArrayList<ContentProviderOperation> opsBatch = new ArrayList<ContentProviderOperation>();
			opsBatch.add(CertDBUtility.getInvalidateOp(hostName, 0));
			final ContentProviderClient providerClient = androidContext.getContentResolver().acquireContentProviderClient(CertDBContent.AUTHORITY);
			ContentProviderResult[] result = providerClient.applyBatch(opsBatch);
			if(result != null && result[0] != null && result[0].count != null) {
				count = result[0].count;
			}
		} catch(Exception e) {
			logger.error("certificate store invalidate failed: ", e);
		}
		
		return count > 0;
	}

    /**
     * Force a mismatch refresh certificate for the given hostname.
     * @param androidContext
     * @param hostName
     * @return
     */
    public static boolean forceCertificateMismatch(Context androidContext, String hostName, String originHash) {
        int count = 0;
        try {
            ArrayList<ContentProviderOperation> opsBatch = new ArrayList<ContentProviderOperation>();
            opsBatch.add(CertDBUtility.getInvalidateOp(hostName, 1, REFRESH_TYPE_MISMATCH, originHash));
            ContentResolver cr = androidContext.getContentResolver();
            final ContentProviderClient providerClient = cr.acquireContentProviderClient(CertDBContent.AUTHORITY);
            ContentProviderResult[] result = providerClient.applyBatch(opsBatch);
            if(result != null && result[0] != null && result[0].count != null) {
                count = result[0].count;
            }
        } catch(Exception e) {
            logger.error("certificate mismatch refresh failed: ", e);
        }
        
        return count > 0;
    }

    public static String getOriginHash(Context androidContext, String hostName) {
        Cursor c = null;
        try {
            c = androidContext.getContentResolver().query(OCCerts.CONTENT_URI, CertDBConstantsIF.PRJ_ORIGIN_HASH,
                    CertDBConstantsIF.SEL_AVAILABLE_OR_INVALID_HOST, new String[] {hostName}, null);
            if(c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(OCCerts.ORIGIN_HASH));
            }
        } catch(Exception e) {
            Log.e("Error in getOriginHash. %s", e.toString());
        } finally {
            if(c != null) {
                c.close();
            }
        }
        return null;
    }    

    public static int getCertificateInvalidationCount(Context androidContext, String hostname) {
        Cursor c = null;
        int count = 0;
        try {
            c = androidContext.getContentResolver().query(OCCerts.CONTENT_URI, PRJ_INVALIDATE_COUNT, 
                    SEL_HOST_NAME, new String[] {hostname}, null);
            if(c != null && c.moveToFirst()) {
                count = c.getInt(c.getColumnIndex(OCCerts.INVALIDATE_COUNT));
            }
        } catch(Exception e) {
            Log.e("Failed to get invalidation count. err=%s", e.toString());
        } finally {
            if(c != null) {
                c.close();
            }
        }

        return count;
    }
}
