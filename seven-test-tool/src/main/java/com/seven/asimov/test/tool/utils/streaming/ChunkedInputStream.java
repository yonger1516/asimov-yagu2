package com.seven.asimov.test.tool.utils.streaming;

import com.seven.asimov.test.tool.constants.Constants;
import com.seven.asimov.test.tool.core.HttpMessageLogger;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Modifications done by Daniel Matuschek (daniel@matuschek.net)
 * - modified JavaDoc documentation
 * - adapted to Java 1.2, removed deprecated DataInputStream.readLine() method
 * - replaced DataInputStream by InputStream (there was no need for a
 *   DatainputStream, not idea why this was used in the original version)
 * - fixed a bug (there is an CRLF after every the data block)
 */

/**
 * An InputStream that implements HTTP/1.1 chunking.
 * <p/>
 * This class lets a Servlet read its request data as an HTTP/1.1 chunked stream. Chunked streams are a way to send
 * arbitrary-length data without having to know beforehand how much you're going to send. They are introduced by a
 * "Transfer-Encoding: chunked" header, so if such a header appears in an HTTP request you should use this class to read
 * any data.
 * <p/>
 * Sample usage: <BLOCKQUOTE>
 * <p/>
 * <PRE>
 * <CODE>
 * InputStream in = req.getInputStream();
 * if ( "chunked".equals( req.getHeader( "Transfer-Encoding" ) ) )
 * in = new ChunkedInputStream( in );
 * </CODE>
 * </PRE>
 * <p/>
 * </BLOCKQUOTE>
 * <p/>
 * Because it would be impolite to make the authors of every Servlet include the above code, this is general done at the
 * server level so that it happens automatically. Servlet authors will generally not create ChunkedInputStreams. This is
 * in contrast with ChunkedOutputStream, which Servlets have to call themselves if they want to use it.
 * <p/>
 * <A HREF="/resources/classes/Acme/Serve/servlet/http/ChunkedInputStream.java">Fetch the software.</A><BR>
 * <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
 *
 * @author Jef Poskanzer
 * @author Daniel Matuschek
 * @version $Id: ChunkedInputStream.java,v 1.6 2002/05/31 14:45:56 matuschd Exp $
 */
public class ChunkedInputStream extends MyInputStream {

    private byte[] mB1 = new byte[1];

    /**
     * number of bytes available in the current chunk.
     */
    private int mChunkCount = 0;

    private Vector<String> mFooterNames = null;
    private Vector<String> mFooterValues = null;
    private ByteArrayOutputStream mRawByteArray = new ByteArrayOutputStream();
    private int mBytesRead;
    private int mContentLength;
    private boolean mComplete;

    public MyInputStream getMyInputStream() {
        return (MyInputStream) in;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public ByteArrayOutputStream getByteArray() {
        return mRawByteArray;
    }

    public void setBytesRead(int bytesRead) {
        this.mBytesRead = bytesRead;
    }

    public int getBytesRead() {
        return mBytesRead;
    }

    /**
     * Make a ChunkedInputStream.
     */
    public ChunkedInputStream(MyInputStream in) {
        super(in);
    }

    /**
     * The FilterInputStream implementation of the single-byte read() method just reads directly from the underlying
     * stream. We want to go through our own read-block method, so we have to override. Seems like FilterInputStream
     * really ought to do this itself.
     */
    public int read() throws IOException {
        if (read(mB1, 0, 1) == -1) {
            return -1;
        }

        return mB1[0];
    }

    /**
     * Reads into an array of bytes.
     *
     * @param b   the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 on EOF
     * @throws IOException if an I/O error has occurred
     */
    public int read(byte[] b, int off, int len) throws IOException {
        if (mChunkCount == 0) {
            startChunk();
            if (mChunkCount == 0) {
                mComplete = true;
                return Constants.EOF;
            }
        }
        int toRead = Math.min(mChunkCount, len);
        int r = in.read(b, off, toRead);

        if (r != Constants.EOF) {
            mChunkCount -= r;
            mBytesRead += r;
            if (mBytesRead <= HttpMessageLogger.MESSAGE_SIZE_HEX_LOGGING_LIMIT) {
                mRawByteArray.write(b, 0, r);
            } else {
                mRawByteArray = null;
            }
        }
        return r;
    }

    /**
     * Reads the start of a chunk.
     */
    private void startChunk() throws IOException {
        String line = readLine();
        if (line.equals(StringUtils.EMPTY)) {
            line = readLine();
        }

        try {
            mChunkCount = Integer.parseInt(line.trim(), 16);
        } catch (NumberFormatException e) {
            throw new IOException("malformed chunk (" + line + ")");
        }
        mContentLength += mChunkCount;
        if (mChunkCount == 0) {
            readFooters();
        }

    }

    /**
     * Reads any footers.
     */
    private void readFooters() throws IOException {
        mFooterNames = new Vector<String>();
        mFooterValues = new Vector<String>();
        String line;
        while (true) {
            line = readLine();
            if (line.length() == 0) {
                break;
            }
            int colon = line.indexOf(':');
            if (colon != -1) {
                String name = line.substring(0, colon).toLowerCase();
                String value = line.substring(colon + 1).trim();
                mFooterNames.addElement(name.toLowerCase());
                mFooterValues.addElement(value);
            }
        }
    }

    /**
     * Returns the value of a footer field, or null if not known. Footers come at the end of a chunked stream, so trying
     * to retrieve them before the stream has given an EOF will return only nulls.
     *
     * @param name the footer field name
     */
    public String getFooter(String name) {
        if (!isDone()) {
            return null;
        }
        int i = mFooterNames.indexOf(name.toLowerCase());
        if (i == -1) {
            return null;
        }
        return (String) mFooterValues.elementAt(i);
    }

    /**
     * Returns an Enumeration of the footer names.
     */
    public Enumeration<String> getFooters() {
        if (!isDone()) {
            return null;
        }
        return mFooterNames.elements();
    }

    /**
     * Returns the size of the request entity data, or -1 if not known.
     */
    public int getContentLength() {
        if (!isDone()) {
            return -1;
        }
        return mContentLength;
    }

    /**
     * Tells whether the stream has gotten to its end yet. Remembering whether you've gotten an EOF works fine too, but
     * this is a convenient predicate. java.io.InputStream should probably have its own isEof() predicate.
     */
    public boolean isDone() {
        return mFooterNames != null;
    }

    /**
     * ChunkedInputStream used DataInputStream.readLine() before. This method is deprecated, therefore we will it
     * replace by our own method. Because the chunk lines only use 7bit ASCII, we can use the system default encoding
     * The data lines itself will not be read using this readLine method but by a block read
     */
    protected String readLine() throws IOException {

        ByteBuffer buff = new ByteBuffer();
        byte b = 0;

        do {
            b = (byte) this.in.read();
            mBytesRead += 1;
            if (mBytesRead <= HttpMessageLogger.MESSAGE_SIZE_HEX_LOGGING_LIMIT) {
                mRawByteArray.write(b);
            } else {
                mRawByteArray = null;
            }
            if (b != Constants.LF) {
                buff.append(b);
            }
        } while ((b != Constants.LF));

        // according to the RFC there must be a CR before the LF, but some
        // web servers don't do this :-(
        byte[] byteBuff = buff.getContent();

        if (byteBuff.length == 0) {
            return StringUtils.EMPTY;
        }

        if (byteBuff[byteBuff.length - 1] != Constants.CR) {
            return new String(byteBuff);
        } else {
            return new String(byteBuff, 0, byteBuff.length - 1);
        }
    }

}

/*********************************************
 * Copyright (c) 2001 by Daniel Matuschek
 *********************************************/

/**
 * A ByteBuffer implements a growable byte array. You can simple add bytes like you do it using a Vector, but internally
 * the buffer is implemented as a real array of bytes. This increases memory usage.
 *
 * @author Daniel Matuschek
 * @version $Id $
 */
class ByteBuffer {

    private static final int INITIALSIZE = 1024;

    private int mUsed = 0;
    private int mSize = 0;
    private byte[] mBuff = null;

    /**
     * Initializes a new ByteBuffer object and creates a temporary buffer array of a predefined initial size. If you
     * want to set your own initial size, use the <code>setSize</code> method after initializing the object.
     */
    public ByteBuffer() {
        mSize = INITIALSIZE;
        mBuff = new byte[INITIALSIZE];
    }

    /**
     * Appends a byte to the end of the buffer
     * <p/>
     * If the currently reserved memory is used, the size of the internal buffer will be doubled. In this case the
     * memory usage will temprary increase by factor 3 because it need a temporary storage for the old data.
     * <p/>
     * Be sure that you have enough heap memory !
     *
     * @param b byte to append
     */
    public void append(byte b) {
        if (mUsed >= mSize) {
            doubleBuffer();
        }

        mBuff[mUsed] = b;
        mUsed++;
    }

    /**
     * @return the number of bytes stored in the buffer
     */
    public int length() {
        return mUsed;
    }

    /**
     * @return the buffer contents as a byte array
     */
    public byte[] getContent() {
        byte[] b = new byte[mUsed];
        for (int i = 0; i < mUsed; i++) {
            b[i] = mBuff[i];
        }
        return b;
    }

    /**
     * removes all contents in the buffer.
     */
    public void clean() {
        mUsed = 0;
    }

    /**
     * Sets the size of the internal buffer to the given value. This is useful, if the size of the data that should be
     * stored is known.
     *
     * @param size size of the buffer in Bytes
     */
    public void setSize(int size) {

        // if we have already used more data, ignore it !
        if (size < mUsed) {
            return;
        }

        mSize = size;

        // create a new (larger) array
        byte[] newBuff = new byte[size];

        // copy contents
        for (int i = 0; i < mUsed; i++) {
            newBuff[i] = mBuff[i];
        }

        mBuff = newBuff;
    }

    /**
     * Print the buffer content as a String (use it for debugging only !).
     *
     * @return a String containing every byte in the buffer as a character
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(mBuff.length);
        for (int i = 0; i < mUsed; i++) {
            sb.append(mBuff[i]);
        }
        return sb.toString();
    }

    /**
     * doubles the size of the internal buffer.
     */
    protected void doubleBuffer() {
        // increase size
        setSize(mSize * 2);
    }
}
