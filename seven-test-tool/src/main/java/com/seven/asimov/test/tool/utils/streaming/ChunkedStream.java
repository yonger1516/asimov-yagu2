package com.seven.asimov.test.tool.utils.streaming;

import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.Pipeline;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ChunkedStream handler.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class ChunkedStream extends BufferedInputStream {
    private ByteArrayOutputStream mByteArray;
    private int mBytesRead;

    public byte[] getByteArray() {
        return mByteArray.toByteArray();
    }

    public int getBytesRead() {
        return mBytesRead;
    }

    public ChunkedStream(InputStream in) throws IOException {
        super(in);
        mBytesRead = 0;
        mByteArray = new ByteArrayOutputStream();
        ByteArrayOutputStream lengthArray = new ByteArrayOutputStream();
        Integer chunklength = null;
        while (true) {
            int c = read();
            if (c == -1) {
                break;
            }
            mBytesRead += 1;
            if (mBytesRead <= Pipeline.ResponseHandler.BODY_SIZE_LIMIT) {
                mByteArray.write(c);
            } else {
                mByteArray = null;
            }
            if (c == Constants.CR) {
                c = read();
                if (c == -1) {
                    break;
                }
                mBytesRead += 1;
                if (mBytesRead <= Pipeline.ResponseHandler.BODY_SIZE_LIMIT) {
                    mByteArray.write(c);
                } else {
                    mByteArray = null;
                }
                if (c == Constants.LF) {
                    if (chunklength != null) {
                        if (chunklength == 0) {
                            break;
                        }
                    }
                    // get Length
                    if (lengthArray.size() != 0) {
                        chunklength = Integer.parseInt(lengthArray.toString(), 16);
                        lengthArray.reset();
                        byte[] temparray = new byte[chunklength];
                        int n = read(temparray);
                        if (n == -1) {
                            break;
                        }
                        mBytesRead += n;
                        mByteArray.write(temparray, 0, n);
                        if (mBytesRead <= Pipeline.ResponseHandler.BODY_SIZE_LIMIT) {
                            mByteArray.write(temparray, 0, n);
                        } else {
                            mByteArray = null;
                        }
                    }
                }
            } else {
                lengthArray.write(c);
            }
        }
    }
}
