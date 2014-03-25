package com.seven.asimov.it.base;

import android.os.Parcel;
import android.os.Parcelable;

public class HttpHeaderField implements Parcelable {

    private String mName;
    private String mValue;
    
    public static final Parcelable.Creator<HttpHeaderField> CREATOR = new Parcelable.Creator<HttpHeaderField>() {
        public HttpHeaderField createFromParcel(Parcel in) {
            return new HttpHeaderField(in);
        }

        public HttpHeaderField[] newArray(int size) {
            return new HttpHeaderField[size];
        }
    };

    public HttpHeaderField() {

    }

    public HttpHeaderField(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        mName = in.readString();
        mValue = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mName);
        out.writeString(mValue);
    }
    
    public HttpHeaderField(String name, String value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public String getValue() {
        return mValue;
    }
}
