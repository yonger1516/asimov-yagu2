package com.seven.asimov.it.utils;

import android.util.Log;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Collection of common I/O related utility methods.
 *
 * @author Maksim Solodovnikov (msolodovnikov@seven.com)
 */
public final class IOUtil {
    /**
     * Hidden constructor.
     */
    private IOUtil() {
        // Empty
    }

    /**
     * Safely closes the provided <code>Closeable</code>.
     *
     * @param c closeable to close.
     */
    public static void safeClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                // Ignored
            }
        }
    }

    /**
     * Safely closes the provided <code>ServerSocket</code>.
     *
     * @param serverSocket ServerSocket to close.
     */
    public static void safeClose(ServerSocket serverSocket){
        if (serverSocket != null){
            try {
                serverSocket.close();
            }catch (Exception e){
                // ignored
            }
        }
    }

    /**
     * Safely closes the provided <code>HttpURLConnection</code> by executing its {@link HttpURLConnection#disconnect()}
     * method.
     *
     * @param c connection to close.
     */
    public static void safeClose(HttpURLConnection c) {
        if (c != null) {
            try {
                c.disconnect();
            } catch (Exception e) {
                // Ignored
            }
        }
    }

    public static void safeClose(Socket s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception e) {
                // Ignored
            }
        }
    }

    /**
     * Transfers data from the provided input stream to the provided output stream, using the provided byte buffer as a
     * temporary storage.
     *
     * @param source      input stream to transfer data from.
     * @param destination output stream to transfer data to.
     * @param buffer      buffer to use as a temporary storage.
     * @throws IOException in case an I/O error occurs.
     */
    public static void transfer(InputStream source, OutputStream destination, byte[] buffer) throws IOException {
        int len;
        while ((len = source.read(buffer)) >= 0) {
            destination.write(buffer, 0, len);
        }
    }

    /**
     * Checks whether the provided URI string is in an absolute form, i.e. has scheme part specified.
     *
     * @param uri URI string to check.
     * @return <code>true</code> in case the provided URI string is in an absolute form, otherwise <code>false</code>.
     */
    public static boolean isAbsoluteUri(String uri) {
        int pos = 0;
        int len = uri.length();
        while (pos < len) {
            switch (uri.charAt(pos++)) {
                case '/':
                case '?':
                case '#':
                    return false;
                case ':':
                    return true;
                default:
            }
        }

        return false;
    }

    /**
     * Checks whether port is a default port for the given scheme.
     *
     * @param scheme http scheme
     * @param port port
     * @return <code>true</code> if port is a default port for the given scheme, <code>false</code> otherwise.
     */
    public static boolean isDefaultPort(String scheme, int port) {
        return (port == 80 && scheme.equals("http")) || (port == 443 && scheme.equals("https"));
    }

    public static void safeCopyFileToDir(String source, String destination) {
        try {
            FileUtils.copyFileToDirectory(new File(source), new File(destination));
        } catch (IOException e) {
            Log.w("IOUtil", "Failed to copy file (" + source + "): " + e.getMessage());
        }
    }

    public static void safeCopyDirectory(String source, String destination) {
        try {
            FileUtils.copyDirectory(new File(source), new File(destination));
        } catch (IOException e) {
            Log.w("IOUtil", "Failed to copy dir (" + source + "): " + e.getMessage());
        }
    }

    public static void safeMoveFile(String source, String destination) {
        try {
            FileUtils.moveFile(new File(source), new File(destination));
        } catch (IOException e) {
            Log.w("IOUtil", "Failed to move file (" + source + "): " + e.getMessage());
        }
    }

    private static Queue<String> filesInDir = new ConcurrentLinkedQueue<String>();

    private static void readDir(String path) {
        File dir = new File(path);
        String[] files = dir.list();
        if (files == null) {
            Log.e("IOUtil","Specified path does not exist or is not a directory!");
            System.exit(0);
        } else {
            for (String fileName : files) {
                if (new File(path + fileName).isFile()) {
                    filesInDir.add(path + fileName);
                }
                if (new File(path + fileName).isDirectory()) {
                    readDir(path + fileName + "/");
                }
            }
        }
    }

    public static Queue<String> getFilesInDir(String path){
        filesInDir.clear();
        readDir(path);
        return new ConcurrentLinkedQueue<String>(filesInDir);
    }
}
