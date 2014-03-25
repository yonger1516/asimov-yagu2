package com.seven.asimov.it.base;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.seven.asimov.it.base.https.CertUtils;
import com.seven.asimov.it.base.https.CertificateWrapper;

import android.os.Parcel;
import android.os.Parcelable;

import com.seven.asimov.it.base.https.CertificateWrapper;

public class HttpResponse implements Parcelable {

    public static class Builder {

        private final HttpResponse mResponse = new HttpResponse();

        public static Builder create() {
            return new Builder();
        }

        public Builder addHeaderField(String name, String value) {
            mResponse.addHeaderField(new HttpHeaderField(name, value));
            return this;
        }

        public Builder addHeaderField(HttpHeaderField field) {
            mResponse.addHeaderField(field);
            return this;
        }

        public Builder setBody(String body) {
            mResponse.setBody(body);
            return this;
        }

        public Builder setBodySkipped(boolean bodySkipped) {
            mResponse.setBodySkipped(bodySkipped);
            return this;
        }

        public Builder setBodyLength(int bodyLength) {
            mResponse.setBodyLength(bodyLength);
            return this;
        }

        public Builder setBodyHash(byte[] bodyHash) {
            mResponse.setBodyHash(bodyHash);
            return this;
        }

        public Builder setStatusCode(int code) {
            mResponse.setStatusCode(code);
            return this;
        }

        public Builder setDuration(long duration) {
            mResponse.setDuration(duration);
            return this;
        }

        public HttpResponse getResponse() {
            return mResponse;
        }

        public Builder setStatusLine(String status) {
            mResponse.setStatusLine(status);
            return this;
        }

        public Builder setRawContent(String response) {
            mResponse.setRawContent(response);
            return this;
        }

        public Builder setSSLProtocol(String sslProtocol) {
            mResponse.setSSLProtocol(sslProtocol);
            return this;
        }

        public Builder setCipherSuite(String cipherSuite) {
            mResponse.setCipherSuite(cipherSuite);
            return this;
        }

        public Builder setCertificates(Certificate[] serverCertificates) {
            mResponse.setCertificates(serverCertificates);
            return this;
        }
    }

    private String mStatusLine;
    private final List<HttpHeaderField> mHeaderFields = new ArrayList<HttpHeaderField>();
    private String mBody;
    private int mBodyLength;
    private byte[] mBodyHash;
    private int mStatusCode;
    private long mDuration;
    private String mRawText;
    private boolean mIsBodySkipped;
    private CertificateWrapper[] mCertificateWrappers;
    private String mSSLProtocol;
    private String mCipherSuite;

    private long mTO;
    private long mD;
    private long mRT;
    private long mIT = -1;

    private boolean isBinaryBody;
    private boolean isFullBodyRecieved;
    private int bodyBytesRecieved;

    public boolean isBinaryBody() {
        return isBinaryBody;
    }

    public void setBinaryBody(boolean isBinaryBody) {
        this.isBinaryBody = isBinaryBody;
    }

    public int getBodyBytesRecieved() {
        return bodyBytesRecieved;
    }

    public void setBodyBytesRecived(int bodyBytesRecieved) {
        this.bodyBytesRecieved = bodyBytesRecieved;
    }

    public int addBodyBytesRecieved(int newBytes) {
        this.bodyBytesRecieved += newBytes;
        return this.bodyBytesRecieved;
    }

    public boolean isFullBodyRecieved() {
        return isFullBodyRecieved;
    }

    public void setFullBodyRecieved(boolean isFullBodyRecieved) {
        this.isFullBodyRecieved = isFullBodyRecieved;
    }

    private static final int BODY_LOG_SIZE = 4096;

    public void addHeaderField(HttpHeaderField field) {
        mHeaderFields.add(field);
    }

    public void setRawContent(String response) {
        mRawText = response;
    }

    public String getHeaderField(String name) {
        for (HttpHeaderField field : mHeaderFields) {
            if (field.getName() != null) {
                if (field.getName().equalsIgnoreCase(name)) {
                    return field.getValue();
                }
            }
        }
        return null;
    }

    public List<HttpHeaderField> getHeaderFields() {
        return mHeaderFields;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public String getBody() {
        return mBody;
    }

    public void setBodyLength(int bodyLength) {
        this.mBodyLength = bodyLength;
    }

    public int getBodyLength() {
        return mBodyLength;
    }

    public void setBodyHash(byte[] bodyHash) {
        this.mBodyHash = bodyHash;
    }

    public byte[] getBodyHash() {
        return mBodyHash;
    }

    public void setBodySkipped(boolean isBodySkipped) {
        mIsBodySkipped = isBodySkipped;
    }

    public boolean isIsBodySkipped() {
        return mIsBodySkipped;
    }

    public void setStatusCode(int statusCode) {
        mStatusCode = statusCode;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setStatusLine(String status) {
        mStatusLine = status;
    }

    public String getStatusLine() {
        return mStatusLine;
    }

    public String getRawContent() {
        return mRawText;
    }

    public String getSSLProtocol() {
        return mSSLProtocol;
    }

    public void setSSLProtocol(String sslProtocol) {
        this.mSSLProtocol = sslProtocol;
    }

    public void setCipherSuite(String cipherSuite) {
        this.mCipherSuite = cipherSuite;
    }

    public String getCipherSuite() {
        return mCipherSuite;
    }

    public void setCertificates(Certificate[] serverCertificates) {
        if (serverCertificates == null)
            return;
        mCertificateWrappers = new CertificateWrapper[serverCertificates.length];
        for (int i = 0; i < serverCertificates.length; i++) {
            mCertificateWrappers[i] = new CertificateWrapper(serverCertificates[i]);
        }
    }

    public CertificateWrapper[] getCertificates() {
        return mCertificateWrappers;
    }

    public String getFullResponse() {
        StringBuilder response = new StringBuilder().append(getStatusLine()).append("\r\n");

        for (HttpHeaderField field : mHeaderFields) {
            response.append(field.getName()).append(": ").append(field.getValue()).append("\r\n");
        }

        response.append("\r\n");

        if (mBody != null) {
            if (mBody.length() <= BODY_LOG_SIZE) {
                response.append(mBody);
            } else {
                response.append(mBody, 0, BODY_LOG_SIZE);
                response.append("... [Body is truncated, total length: ");
                response.append(mBody.length());
                response.append(" characters]");
            }
            response.append("\r\n");
        }

        if (mIsBodySkipped) {
            response.append("[Body is skipped...]\r\n");
            response.append("[Skipped body size]: " + mBodyLength + "\r\n");
            response.append("[Skipped body hash]: " + (mBodyHash == null ? "" : Arrays.toString(mBodyHash)) + "\r\n");
        }

        if (mCipherSuite != null) {
            response.append("[SSL Session]: ");
            if (mSSLProtocol != null)
                response.append("Protocol: " + mSSLProtocol + ", ");
            response.append("Cipher Suite: " + mCipherSuite);
            response.append("\r\n");
        }

        if (mCertificateWrappers != null) {
            for (CertificateWrapper c : getCertificates()) {
                response.append("[Certificate]: V" + c.getVersion() + "; Public key length: " + c.getPublicKeyLength()
                        + "; Sign. alg: " + c.getSigAlgName() + "; Subject: " + c.getSubject() + "; Issuer: "
                        + c.getIssuer() + "; Serial Number: " + c.getSerialNumber().toString(16) + "; CA: "
                        + c.getBasicConstraints());
                if (c.getAuthorityKeyIdDer() != null) {
                    response.append("; Auth key id (der): " + CertUtils.bytesToHexString(c.getAuthorityKeyIdDer()));
                }
                if (c.getSubjectKeyIdDer() != null) {
                    response.append("; Subj key id (der): " + CertUtils.bytesToHexString(c.getSubjectKeyIdDer()));
                }
                if (c.getBasicConstraintsDer() != null) {
                    response.append("; Basic constraints (der): "
                            + CertUtils.bytesToHexString(c.getBasicConstraintsDer()));
                }
                if (c.getKeyUsage() != null) {
                    response.append("; KeyUsage");
                    if (c.isKeyUsageCritical())
                        response.append("(Critical)");
                    response.append(": ");
                    response.append(c.getKeyUsageAsString());
                }
                response.append("; Not before: " + c.getNotBefore());
                response.append("; Not after: " + c.getNotAfter());

                String s = "";
                try {
                    s = c.getSubjectAlternativeNamesAsString();
                } catch (Exception e) {
                    e.printStackTrace();
                    s = "Cannot parse: " + e;
                }
                if (s.length() > 0) {
                    response.append("; Alt Names");
                    if (c.isSubjectAlternativeNamesCritical())
                        response.append("(Critical)");
                    response.append(": ");
                    response.append(s);
                }

                response.append("\r\n");
            }
        }

        return response.toString();
    }

    public String getFullResponseWithoutCLRF() {
        StringBuilder response = new StringBuilder().append(getStatusLine()).append("\r\n");

        for (HttpHeaderField field : mHeaderFields) {
            if (field.getName() != null)
                response.append(field.getName()).append(": ").append(field.getValue()).append("\r\n");
            else
                response.append(field.getValue()).append("\r\n");
        }

        response.append("\r\n");

        if (mBody != null) {
            if (mBody.length() <= BODY_LOG_SIZE) {
                response.append(mBody);
            } else {
                response.append(mBody, 0, BODY_LOG_SIZE);
                response.append("... [Body is truncated, total length: ");
                response.append(mBody.length());
                response.append(" characters]");
            }
            // response.append("\r\n");
        }

        if (mIsBodySkipped) {
            response.append("[Body is skipped...]\r\n");
            response.append("[Skipped body size]: " + mBodyLength + "\r\n");
            response.append("[Skipped body hash]: " + (mBodyHash == null ? "" : Arrays.toString(mBodyHash)) + "\r\n");
        }

        if (mCipherSuite != null) {
            response.append("[SSL Session]: ");
            if (mSSLProtocol != null)
                response.append("Protocol: " + mSSLProtocol + ", ");
            response.append("Cipher Suite: " + mCipherSuite);
            response.append("\r\n");
        }

        if (mCertificateWrappers != null) {
            for (CertificateWrapper c : getCertificates()) {
                response.append("[Certificate]: V" + c.getVersion() + "; Public key length: " + c.getPublicKeyLength()
                        + "; Sign. alg: " + c.getSigAlgName() + "; Subject: " + c.getSubject() + "; Issuer: "
                        + c.getIssuer() + "; Serial Number: " + c.getSerialNumber().toString(16) + "; CA: "
                        + c.getBasicConstraints());
                if (c.getAuthorityKeyIdDer() != null) {
                    response.append("; Auth key id (der): " + CertUtils.bytesToHexString(c.getAuthorityKeyIdDer()));
                }
                if (c.getSubjectKeyIdDer() != null) {
                    response.append("; Subj key id (der): " + CertUtils.bytesToHexString(c.getSubjectKeyIdDer()));
                }
                if (c.getBasicConstraintsDer() != null) {
                    response.append("; Basic constraints (der): "
                            + CertUtils.bytesToHexString(c.getBasicConstraintsDer()));
                }
                if (c.getKeyUsage() != null) {
                    response.append("; KeyUsage");
                    if (c.isKeyUsageCritical())
                        response.append("(Critical)");
                    response.append(": ");
                    response.append(c.getKeyUsageAsString());
                }
                response.append("; Not before: " + c.getNotBefore());
                response.append("; Not after: " + c.getNotAfter());

                String s = "";
                try {
                    s = c.getSubjectAlternativeNamesAsString();
                } catch (Exception e) {
                    e.printStackTrace();
                    s = "Cannot parse: " + e;
                }
                if (s.length() > 0) {
                    response.append("; Alt Names");
                    if (c.isSubjectAlternativeNamesCritical())
                        response.append("(Critical)");
                    response.append(": ");
                    response.append(s);
                }

                response.append("\r\n");
            }
        }

        return response.toString();
    }

    private NetStat.SocketInfo socketInfo;
    private long startTime;

    public static final Parcelable.Creator<HttpResponse> CREATOR = new Parcelable.Creator<HttpResponse>() {
        public HttpResponse createFromParcel(Parcel in) {
            return new HttpResponse(in);
        }

        public HttpResponse[] newArray(int size) {
            return new HttpResponse[size];
        }
    };

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public NetStat.SocketInfo getSocketInfo() {
        return socketInfo;
    }

    public void setSocketInfo(NetStat.SocketInfo socketInfo) {
        this.socketInfo = socketInfo;
    }

    public HttpResponse() {

    }

    public HttpResponse(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        mStatusLine = in.readString();
        in.readList(mHeaderFields, HttpResponse.class.getClassLoader());
        mBody = in.readString();
        mBodyLength = in.readInt();
        mStatusCode = in.readInt();
        mDuration = in.readLong();
        mRawText = in.readString();
        socketInfo = (NetStat.SocketInfo) in.readParcelable(NetStat.SocketInfo.class.getClassLoader());
        startTime = in.readLong();
        mTO = in.readLong();
        mD = in.readLong();
        mRT = in.readLong();
        mIT = in.readLong();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mStatusLine);
        out.writeList(mHeaderFields);
        out.writeString(mBody);
        out.writeInt(mBodyLength);
        out.writeInt(mStatusCode);
        out.writeLong(mDuration);
        out.writeString(mRawText);
        out.writeParcelable(socketInfo, flags);
        out.writeLong(startTime);
        out.writeLong(mTO);
        out.writeLong(mD);
        out.writeLong(mRT);
        out.writeLong(mIT);
    }

    public String getShortResponse() {
        StringBuilder response = new StringBuilder().append(getStatusLine()).append("\r\n");

        for (HttpHeaderField field : mHeaderFields) {
            response.append(field.getName()).append(": ").append(field.getValue()).append("\r\n");
        }

        response.append("[No body displayed]");

        return response.toString();
    }

    public long getTO() {
        return mTO;
    }

    public void setTO(long TO) {
        this.mTO = TO;
    }

    public long getD() {
        return mD;
    }

    public void setD(long D) {
        this.mD = D;
    }

    public long getRT() {
        return mRT;
    }

    public void setRT(long RT) {
        this.mRT = RT;
    }

    public long getIT() {
        return mIT;
    }

    public void setIT(long IT) {
        this.mIT = IT;
    }

    public long getRI() {
        if (mIT == -1)
            return -1;
        else
            return mD + mRT + mIT;
    }

}
