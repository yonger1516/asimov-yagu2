package com.seven.asimov.it.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class HttpRequest implements Parcelable {

    public static class Builder {

        private final HttpRequest mRequest = new HttpRequest();

        public static Builder create() {
            return new Builder();
        }

        public Builder setUri(String uri) {
            mRequest.setUri(uri);
            return this;
        }

        public Builder addHeaderField(String name, String value) {
            mRequest.addHeaderField(new HttpHeaderField(name, value));
            return this;
        }

        public Builder setMethod(String method) {
            mRequest.setMethod(method);
            return this;
        }

        public Builder setChunkSize(int chunkSize) {
            mRequest.setChunkSize(chunkSize);
            return this;
        }

        public Builder setBody(String body) {
            mRequest.setBody(body);
            return this;
        }

        public HttpRequest getRequest() {
            return mRequest;
        }

        public Builder setFollowRedirects(boolean followRedirects) {
            mRequest.setFollowRedirects(followRedirects);
            return this;
        }
    }

    private final List<HttpHeaderField> mHeaderFields = new ArrayList<HttpHeaderField>();
    private String mMethod = "GET";
    private String mUri;
    private int mChunkSize;
    private String mBody;
    private boolean mFollowRedirects = true;
    private long mSendTime;

    public static final Parcelable.Creator<HttpRequest> CREATOR = new Parcelable.Creator<HttpRequest>() {
        public HttpRequest createFromParcel(Parcel in) {
            return new HttpRequest(in);
        }

        public HttpRequest[] newArray(int size) {
            return new HttpRequest[size];
        }
    };

    public HttpRequest() {
    }

    public HttpRequest copy() {
        HttpRequest copy = new HttpRequest();
        for (HttpHeaderField field : mHeaderFields) {
            copy.addHeaderField(field);
        }
        copy.setMethod(mMethod);
        copy.setUri(mUri);
        copy.setChunkSize(mChunkSize);
        copy.setBody(mBody);
        copy.setFollowRedirects(mFollowRedirects);
        copy.setSendTime(mSendTime);
        return copy;
    }

    public HttpRequest(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        in.readList(mHeaderFields, HttpHeaderField.class.getClassLoader());
        mMethod = in.readString();
        mUri = in.readString();
        mChunkSize = in.readInt();
        mBody = in.readString();
        mFollowRedirects = in.readInt() == 0;
        mSendTime = in.readLong();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeList(mHeaderFields);
        out.writeString(mMethod);
        out.writeString(mUri);
        out.writeInt(mChunkSize);
        out.writeString(mBody);
        out.writeInt(mFollowRedirects ? 0 : 1);
        out.writeLong(mSendTime);
    }

    public void addHeaderField(HttpHeaderField field) {
        mHeaderFields.add(field);
    }

    public String getHeaderField(String name) {
        for (HttpHeaderField field : mHeaderFields) {
            if (field.getName().equalsIgnoreCase(name)) {
                return field.getValue();
            }
        }
        return null;
    }

    public List<HttpHeaderField> getHeaderFields() {
        return mHeaderFields;
    }

    public void setMethod(String method) {
        mMethod = method;
    }

    public String getMethod() {
        return mMethod;
    }

    public void setChunkSize(int chunkSize) {
        mChunkSize = chunkSize;
    }

    public int getChunkSize() {
        return mChunkSize;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public String getBody() {
        return mBody;
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public String getUri() {
        return mUri;
    }

    public void setFollowRedirects(boolean followRedirects) {
        mFollowRedirects = followRedirects;
    }

    public boolean getFollowRedirects() {
        return mFollowRedirects;
    }

    public String getFullRequest() {
        StringBuilder request = new StringBuilder(mMethod).append(" ").append(mUri).append(" HTTP/1.1").append("\r\n");

        for (HttpHeaderField field : mHeaderFields) {
            request.append(field.getName()).append(": ").append(field.getValue()).append("\r\n");
        }

        request.append("\r\n");

        if (mBody != null) {
            request.append(mBody);
        }

        return request.toString();
    }

    public String getIncorrectRequest() {
        StringBuilder request = new StringBuilder(mMethod).append(" ");

        request.append(mUri).append(" HTTP/2.1").append("\r\n");

        for (HttpHeaderField field : mHeaderFields) {
            request.append(field.getName()).append(": ").append(field.getValue()).append("\r\n");
        }

        request.append("\r\n");

        if (mBody != null) {
            request.append(mBody);
        }

        return request.toString();
    }

    public String getFullRequest(boolean isHttp11) {
        StringBuilder request = new StringBuilder(mMethod).append(" ");
        if (isHttp11) {
            URL url;
            try {
                url = new URL(mUri);

                request.append((url.getFile() == "" ? "/" : url.getFile()))
                        .append(url.getRef() == null ? "" : "#" + url.getRef()).append(" HTTP/1.1").append("\r\n")
                        .append("Host").append(": ").append(url.getHost()).append("\r\n");
            } catch (MalformedURLException e) {
                request.append(mUri).append(" HTTP/1.1").append("\r\n");
            }
        } else {
            request.append(mUri).append(" HTTP/1.0").append("\r\n");
        }

        for (HttpHeaderField field : mHeaderFields) {
            request.append(field.getName()).append(": ").append(field.getValue()).append("\r\n");
        }

        request.append("\r\n");

        if (mBody != null) {
            request.append(mBody);
        }

        return request.toString();
    }

    public long getSendTime() {
        return mSendTime;
    }

    public void setSendTime(long sendTime) {
        this.mSendTime = sendTime;
    }

}
