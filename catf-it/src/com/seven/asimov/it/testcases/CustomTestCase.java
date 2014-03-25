package com.seven.asimov.it.testcases;

import android.util.Log;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.base.TcpDumpTestCase;
import com.seven.asimov.it.utils.IOUtil;
import com.seven.asimov.it.utils.TestUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

public class CustomTestCase extends TcpDumpTestCase {
    private static final String TAG = CustomTestCase.class.getSimpleName();
    protected static long requestNumber = 1;
    protected static final long RI = 2500;

    protected void sendQueue(int SOCKET_TIMEOUT, String host, HttpRequest request,
                             boolean ssl,
                             boolean useRandomHeader, int requestCount) throws Throwable {
        sendQueue(SOCKET_TIMEOUT, host, request, ssl, useRandomHeader, requestCount, HTTP_VERSION.HTTP11);

    }
    protected void sendQueue(int SOCKET_TIMEOUT, String host, HttpRequest request,
                             boolean ssl,
                             boolean useRandomHeader, int requestCount, HTTP_VERSION version) throws Throwable {
        for (int i = 0; i < requestCount; i++) {
            try {
                sendRequestApacheSSL(request, host, ssl, SOCKET_TIMEOUT, useRandomHeader, version);
                TestUtil.sleep(RI);
            } catch (Throwable e) {
            }
        }
    }

    public static HttpResponse sendRequestApacheSSL(HttpRequest request_orig, String host,
                                                    boolean ssl,
                                                    int readTimeout, boolean useRandomHeader) throws KeyManagementException, NoSuchAlgorithmException {
        return sendRequestApacheSSL(request_orig, host, ssl, readTimeout, useRandomHeader, HTTP_VERSION.HTTP11);
    }

    public static HttpResponse sendRequestApacheSSL(HttpRequest request_orig, String host, boolean ssl,
                                                    int readTimeout, boolean useRandomHeader, HTTP_VERSION http_version) throws NoSuchAlgorithmException, KeyManagementException {
        Socket socket = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        InputStream in = null;

        SSLSocketFactory mSSLSocketFactory = null;
        SSLContext mSslContext;

        if (ssl) {
            mSslContext = SSLContext.getInstance("SSL");
            TrustManager[] tm = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};
            mSslContext.init(null, tm, null);
            mSSLSocketFactory = mSslContext.getSocketFactory();
        }

        HttpRequest copy = request_orig.copy();
        if (useRandomHeader) {
            copy.addHeaderField(new HttpHeaderField("Random-header", UUID.randomUUID().toString()));
        }

        byte[] request;
        if(http_version == HTTP_VERSION.HTTP11) {
            request = copy.getFullRequest(true).getBytes();
        } else if(http_version == HTTP_VERSION.HTTP10) {
            request = copy.getFullRequest(false).getBytes();
        } else {
            request = copy.getIncorrectRequest().getBytes();
        }

        try {
            System.out.print("Sending request nr. ");
            System.out.print(String.valueOf(requestNumber));
            long reqNum = requestNumber;
            ++requestNumber;
            if (mSSLSocketFactory != null) {
                Log.v(TAG, " using SSL");
            }
            Log.v(TAG, " through OC:");
            Log.v(TAG, new String(request));
            long start = System.currentTimeMillis();

            if (mSSLSocketFactory == null) {
                socket = new Socket(host, 80);
            } else {
                socket = mSSLSocketFactory.createSocket(host, 443);
            }

            socket.setSoTimeout(readTimeout);

            dos = new DataOutputStream(socket.getOutputStream());
            dos.write(request);
            dos.flush();

            in = socket.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;

            // OC sends RST instead of FIN, that causes SocketException. So, handle it as end of input.
            try {
                while ((len = in.read(buf)) >= 0) {
                    baos.write(buf, 0, len);
                }
            } catch (SocketException e) {
                Log.e(TAG, "SocketException, ignored. message: " + e.getMessage());
            } catch (SocketTimeoutException e) {
                Log.e(TAG, "SocketTimeoutException, ignored. message: " + e.getMessage());
            }

            StringBuilder sb = new StringBuilder();
            sb.append(baos.toString());

            long duration = System.currentTimeMillis() - start;

            HttpResponse response = buildResponse(sb);
            response.setDuration(duration);

            Log.v(TAG, "Response for request nr. " + reqNum + " received:");
            Log.v(TAG, response.getFullResponse());

            return response;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.safeClose(dis);
            IOUtil.safeClose(dos);
            IOUtil.safeClose(in);
            IOUtil.safeClose(socket);
        }
        HttpResponse badResponse = new HttpResponse();
        badResponse.setStatusCode(-1);
        badResponse.setRawContent("");
        return badResponse;
    }
}
