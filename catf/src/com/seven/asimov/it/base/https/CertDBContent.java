package com.seven.asimov.it.base.https;

import android.net.Uri;
import android.provider.BaseColumns;

public class CertDBContent {

	public static final String AUTHORITY = "com.seven.provider.asimov" + ".CertificatesProvider";
	
	// Site Certificates
	public interface SiteCertColumns extends BaseColumns {
		
		/**
		 * STRING
		 */
		public static final String HOST_NAME = "host_name";
		
		/**
		 * INT. See {@link Status}
		 */
		public static final String STATUS = "status";

		/**
		 * LONG
		 */
		public static final String SAVED_TIME = "saved_time";
		
		/**
		 * LONG
		 */
		public static final String NOT_BEFORE = "not_before";
		
		/**
		 * LONG
		 */
		public static final String NOT_AFTER = "not_after";
		
		/**
		 * LONG
		 */
		public static final String LAST_ACCESS_TIME = "last_access";
				
		/**
		 * STRING
		 */
		public static final String ORIGIN_HASH = "origin_hash";
		
		/**
		 * INT
		 */
		public static final String EXCEPTIONS = "exceptions";
		
		/**
		 * INT
		 */
		public static final String INVALIDATE_COUNT = "invalidate_count";
		
		/**
		 * INT
		 */
		public static final String ERROR_COUNT = "error_count";
		
		/**
		 * BLOB
		 */
		public static final String CERTIFICATE = "certificate";
		
		/**
		 * BLOB
		 */
		public static final String PR_KEY = "key";
		
		/**
		 * TEXT
		 */
		public static final String CIPHER_SUITE = "cipher_suite";
		
		/**
		 * BLOB
		 */
		public static final String E_CACHE_KEY = "e_cache_key";

		public static final String CERTIFICATE_ID = "certificate_id";

        /**
         * TEXT
         */
        public static final String REFRESH_HINTS = "refresh_hints";
	}
	
	
	// Issuer Certificate & Version
	// Do we really need this table ?
	public interface IssuerCertColumns extends BaseColumns {
		
		/**
		 * INT
		 */
		public static final String VERSION = "version";
		
		/**
		 * LONG
		 */
		public static final String VALID_UNTIL = "valid_until";
		
		/**
		 * BLOB
		 */
		public static final String CERTIFICATE = "certificate";
	}
	
	public interface Status {
		public static final int NEW = 1;
		public static final int PENDING = 2;
		public static final int AVAILABLE = 3;
		public static final int PREDOWNLOAD = 4;
		public static final int PREDOWNLOAD_PENDING = 5;
		public static final int INVALID = 6;
	}
	
	public static class OCCerts implements SiteCertColumns {
		
		private OCCerts() {}
		
		private static final String URI_PREFIX = "content://" + AUTHORITY + "/";
		public static final String PATH = "occerts";
		public static final String CERT_COUNT = "certcount";
		
        public static final Uri CONTENT_URI = Uri.parse(URI_PREFIX + PATH);
        public static final Uri CONTENT_URI_COUNT = Uri.parse(URI_PREFIX + CERT_COUNT);
        
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.seven." + PATH;
	}
}
