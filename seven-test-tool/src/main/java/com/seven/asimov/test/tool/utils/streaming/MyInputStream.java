package com.seven.asimov.test.tool.utils.streaming;

import com.seven.asimov.test.tool.constants.Constants;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * MyInputStream.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class MyInputStream extends FilterInputStream {

    private ByteArrayInputStream mBufferedInputStream;

    public ByteArrayInputStream getBufferedInputStream() {
        return mBufferedInputStream;
    }

    public void setBufferedInputStream(byte[] buf, int offset, int length) {
        mBufferedInputStream = new ByteArrayInputStream(buf, offset, length);
    }

    public int getRealBytesRead() {
        return mRealBytesRead;
    }

    private int mRealBytesRead;

    /**
     * Make MyInputStream.
     */
    public MyInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        mRealBytesRead = 0;
        int result;
        if (mBufferedInputStream != null) {
            result = mBufferedInputStream.read();
            if (mBufferedInputStream.available() == 0) {
                mBufferedInputStream = null;
            }
            if (result != Constants.EOF) {
                return result;
            } else {
                mBufferedInputStream = null;
            }
        }
        result = super.read();
        mRealBytesRead = result;
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        mRealBytesRead = 0;
        int result;
        if (mBufferedInputStream != null) {
            result = mBufferedInputStream.read(b, off, len);
            if (mBufferedInputStream.available() == 0) {
                mBufferedInputStream = null;
            }
            if (result != Constants.EOF) {
                return result;
            } else {
                mBufferedInputStream = null;
            }
        }
        result = super.read(b, off, len);
        mRealBytesRead = result;
        return result;
    }
}
