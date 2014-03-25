package com.seven.asimov.it.utils.logcat;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;


enum BLOCK_TYPE {MEM, FILE}

class BlockManager {
    private int blockSize;
    private LinkedList<ByteBlock> blocksReadyForRead;
    private LinkedList<ByteBlock> reuseableBlocks;
    private FileChannel swapFileChannel;
    private Semaphore blocksInWorkCount;
    int nextFreeFileBlockOffset = 0;
    int maxMemBlocksCount = 0;
    AtomicInteger currentMemBlockCount = new AtomicInteger();

    /**
     * Block manager constructor.
     *
     * @param blockSize     Size of one block to use for caching.
     * @param memoryLimit   Total size of memory allowed to use.
     * @param swapFilesPath Path for swap file.
     * @throws Exception
     */
    public BlockManager(int blockSize, int memoryLimit, String swapFilesPath) throws Exception {
        if (blockSize < 8 * 1024)
            throw new Exception("blockSize requested is less than 8K");
        if (memoryLimit < 512 * 1024)
            throw new Exception("memoryLimit requested is less than 512K");

        maxMemBlocksCount = (memoryLimit / (blockSize)) - 2;
        if (maxMemBlocksCount < 2)
            throw new Exception("Specified memory limit of " + memoryLimit + "bytes is not big enough to hold 4 blocks with size of " + blockSize + " bytes");

        this.blockSize = blockSize;
        blocksReadyForRead = new LinkedList<ByteBlock>();
        reuseableBlocks = new LinkedList<ByteBlock>();
        blocksInWorkCount = new Semaphore(0);

        swapFileChannel = (new RandomAccessFile(swapFilesPath + UUID.randomUUID().toString().replace('-', '_') + ".swap", "rw")).getChannel();

    }

    /**
     * Returns new block of specified type. If file block is requested,
     * previously created block can be reused in order to keep swap file size low.
     *
     * @param type Block type to return;
     * @return {@link ByteBlock}
     */
    private ByteBlock getNewBlock(BLOCK_TYPE type) {
        ByteBlock newBlock = null;
        if (type == BLOCK_TYPE.FILE) {
            synchronized (reuseableBlocks) {
                if (!reuseableBlocks.isEmpty())
                    newBlock = reuseableBlocks.removeFirst();
            }
            if (newBlock == null) {
                newBlock = new FileByteBlock(swapFileChannel, nextFreeFileBlockOffset, blockSize);
                nextFreeFileBlockOffset += blockSize;
            }
        } else {
            newBlock = new MemoryByteBlock(blockSize);
        }
        return newBlock;
    }

    /**
     * Returns new {@link ByteBlock} ready for write. Returned block is intended for use by Writer.
     * Also releases given block to be used by Reader.
     *
     * @param currentBlock Currently used block. Will be prepared for read.
     * @return {@link ByteBlock}
     * @throws Exception
     */
    public ByteBlock requestNextWritableBlock(ByteBlock currentBlock) throws Exception {
        ByteBlock nextBlock = null;
        if (currentBlock != null)
            currentBlock.release();

        if (currentMemBlockCount.get() < maxMemBlocksCount) {
            nextBlock = getNewBlock(BLOCK_TYPE.MEM);
            currentMemBlockCount.incrementAndGet();
        } else {
            nextBlock = getNewBlock(BLOCK_TYPE.FILE);
        }

        nextBlock.prepare();
        synchronized (blocksReadyForRead) {
            blocksReadyForRead.addLast(nextBlock);
            blocksInWorkCount.release();
        }

        return nextBlock;
    }

    /**
     * Returns {@link ByteBlock} ready for read. Returned block is intended for use by Reader.
     * Also releases given block to be used by Writer or frees it in case of {@link MemoryByteBlock}.
     *
     * @param currentBlock Currently used block. Will be released.
     * @return {@link ByteBlock}
     * @throws Exception
     */
    public ByteBlock requestNextReadableBlock(ByteBlock currentBlock, boolean EOF) throws Exception {
        ByteBlock nextBlock;

        if (currentBlock != null) {
            currentBlock.release();
            if (currentBlock.getType() == BLOCK_TYPE.FILE) {
                synchronized (reuseableBlocks) {
                    reuseableBlocks.addLast(currentBlock);
                }
            } else if (currentBlock.getType() == BLOCK_TYPE.MEM) {
                currentMemBlockCount.decrementAndGet();
            }
        }

        if (EOF && (blocksInWorkCount.availablePermits() == 0))
            return null;

        blocksInWorkCount.acquire();
        synchronized (blocksReadyForRead) {
            nextBlock = blocksReadyForRead.removeFirst();
        }
        nextBlock.prepare();
        return nextBlock;
    }

    public void close() throws IOException {
        swapFileChannel.close();
        blocksInWorkCount.drainPermits();
        blocksReadyForRead.clear();
    }
}

/**
 * File backed ByteBlock. MappedByteBuffer to store it's content.
 * Allows reuse of blocks, by simply mapping them to specified file block.
 */
class FileByteBlock extends ByteBlock {
    protected FileChannel backingFile;
    protected int blockOffset;


    public FileByteBlock(FileChannel backingFile, int blockOffset, int blockSize) {
        super(BLOCK_TYPE.FILE, blockSize);
        this.blockOffset = blockOffset;
        this.backingFile = backingFile;
    }

    /**
     * Maps file block to memory. Increments usage counter to control block usage.
     * Duplicates created buffer object to allow simultaneous write an read (doesn't double memory usage, both objects share same buffer).
     *
     * @throws Exception
     */
    @Override
    public void prepare() throws Exception {
        synchronized (this) {
            usageCount++;
        }

        if (buffer == null) {
            buffer = backingFile.map(FileChannel.MapMode.READ_WRITE, blockOffset, blockSize);
            readBuffer = buffer.duplicate();
        }
    }

    /**
     * Forces all block data to file. And releases buffer in case it is not already used by Reader (usageCount>0)
     */
    @Override
    public void release() {
        ((MappedByteBuffer) buffer).force();
        synchronized (this) {
            usageCount--;
            if (usageCount == 0) {
                buffer = null;
                readBuffer = null;
                readPosition = 0;
            }
        }
    }
}

/**
 * Simple memory block.
 */
class MemoryByteBlock extends ByteBlock {
    public MemoryByteBlock(int blockSize) {
        super(BLOCK_TYPE.MEM, blockSize);
    }

    @Override
    public void prepare() throws Exception {
        if (buffer == null) {
            buffer = ByteBuffer.allocate(blockSize);
            readBuffer = buffer.duplicate();
        }
    }
}

abstract class ByteBlock {
    protected ByteBuffer buffer;
    protected ByteBuffer readBuffer;
    protected Semaphore bytesAvailable;
    protected int readPosition;
    protected int blockSize;
    protected int usageCount;
    protected BLOCK_TYPE type;


    protected ByteBlock(BLOCK_TYPE type, int blockSize) {
        bytesAvailable = new Semaphore(0);
        readPosition = 0;
        usageCount = 0;
        this.blockSize = blockSize;
        this.type = type;
    }


    public BLOCK_TYPE getType() {
        return type;
    }

    /**
     * Reads one byte from buffer.
     *
     * @return
     * @throws InterruptedException
     */
    public int read() throws InterruptedException {
        if (readPosition < buffer.capacity()) {
            bytesAvailable.acquire();
            return (readBuffer.get()) & 0xff;
        } else
            return -1;
    }

    /**
     * Reads given number of bytes or all bytes left in buffer, depending which value is less.
     * Uses bytesAvailable semaphore to control number of bytes available and block if no more data present in buffer.
     *
     * @param dstBuffer  Destination buffer.
     * @param byteOffset Buffer offset.
     * @param byteCount  Maximum number of bytes to read.
     * @return Number of bytes read.
     * @throws InterruptedException
     */
    public int read(byte[] dstBuffer, int byteOffset, int byteCount, boolean EOF) throws InterruptedException {
        if (byteCount < 1)
            return 0;

        if (readPosition < buffer.capacity()) {
            if (EOF && (bytesAvailable.availablePermits() == 0))
                return -1;

            bytesAvailable.acquire();

            int bytesToRead = Math.min(bytesAvailable.availablePermits() + 1, byteCount);
            bytesAvailable.acquire(bytesToRead - 1);

            readBuffer.get(dstBuffer, byteOffset, bytesToRead);
            readPosition += bytesToRead;
            return bytesToRead;
        } else
            return -1;
    }

    /**
     * Writes data from given channel to current buffer.
     *
     * @param channel Channel to read from.
     * @return Number of free slots left in current buffer. Or -1 if reached channel reached EOF.
     * @throws IOException
     */
    public int write(ReadableByteChannel channel) throws IOException {
        int bytesWritten = channel.read(buffer);
        if (bytesWritten > 0) {
            bytesAvailable.release(bytesWritten);
            return buffer.remaining();
        }

        return -1;
    }

    public int available() {
        return bytesAvailable.availablePermits();
    }

    /**
     * Should be overridden to perform buffer preparing.
     *
     * @throws Exception
     */
    public void prepare() throws Exception {

    }

    /**
     * Should be overridden to perform buffer free
     */
    public void release() {

    }
}

public class BufferBackedInputStream extends InputStream {
    final BlockManager manager;
    final ReadableByteChannel readChannel;
    ByteBlock currentReadBlock;
    boolean interrupted = false;
    InputStream wrappedStream;

    private Thread writerThread = new Thread() {
        @Override
        public void run() {
            Thread.currentThread().setName("Writer thread");
            ByteBlock writeBlock = null;
            int writeRes;
            Log.v("###", "Writer thread started");
            while (true) {
                try {
                    writeBlock = manager.requestNextWritableBlock(writeBlock);
                    do {
                        writeRes = writeBlock.write(readChannel);
                    } while (writeRes > 0);

                    if (writeRes < 0) {
                        break;
                    }
                } catch (Exception e) {
                    Log.v("###DEBUG", "Writer thread exception " + e.toString());
                    return;
                }
            }
            Log.v("###DEBUG", "Writer thread exited");
        }
    };

    public BufferBackedInputStream(InputStream wrappedStream, int blockSize, int memoryLimit, String swapPath) throws Exception {
        manager = new BlockManager(blockSize, memoryLimit, swapPath);
        this.wrappedStream = wrappedStream;
        readChannel = Channels.newChannel(wrappedStream);
        currentReadBlock = null;
        writerThread.start();

        currentReadBlock = manager.requestNextReadableBlock(currentReadBlock, false);

    }

    @Override
    public int available() throws IOException {
        return currentReadBlock.available();
    }

    @Override
    public void close() throws IOException {
        manager.close();
    }

    @Override
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        byte[] buf = new byte[1];
        return read(buf, 0, 1);
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (buffer == null)
            return 0;

        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        int readResult = 0;
        if (offset < 0 || length < 0 || offset + length > buffer.length)
            throw new IndexOutOfBoundsException();

        try {
            do {
                try {
                    readResult = currentReadBlock.read(buffer, offset, length, interrupted);//returns -1 if no more data in current block
                    if (readResult < 0) {
                        currentReadBlock = manager.requestNextReadableBlock(currentReadBlock, interrupted);//returns null if no more blocks to read and EOF is set (Thread was interrupted)
                        if (currentReadBlock == null)
                            return -1;
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            } while (readResult < 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return readResult;
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void stopReading() {
        try {
            wrappedStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
