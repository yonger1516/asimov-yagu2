package com.seven.asimov.it.utils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.apache.commons.lang.StringUtils;

import java.io.*;

public class SSHUtil {
    private Connection conn;
    private boolean isLogined;
    private BufferedReader br = null;
    private OutputStream out = null;
    private static final String end = "^^^^^^^^^^^^^End^^^^^^^^^^^^^^^^^";
    private static final String endCommand = "\necho \"" + end + "\"\n";
    public static SSHUtil sshUtil = new SSHUtil();

    private SSHUtil() {
    }

    public void login(String host, String user, String pwd, String publicKey) throws IOException {
        conn = new Connection(host);
        conn.connect();
        if (pwd == null || StringUtils.isBlank(pwd)) {
            File publicKeyFile = new File(publicKey);
            if (!conn.authenticateWithPublicKey(user, publicKeyFile, pwd)) {
                System.out.println("Authentication failed.");
                throw new IOException("Authentication failed.");
            }
        } else if (!conn.authenticateWithPassword(user, pwd)) {
            System.out.println("Authentication failed.");
            throw new IOException("Authentication failed.");
        }
        isLogined = true;
    }

    public void login(String host, String user, String pwd) throws IOException {
        conn = new Connection(host);
        conn.connect();
        if (!conn.authenticateWithPassword(user, pwd)) {
            System.out.println("Authentication failed.");
            return;
        }
        isLogined = true;
    }

    public String execute(String cmd) throws IOException {
        if (!isLogined) throw new IOException("Authentication failed.");
        Session sess = null;
        try {
            sess = conn.openSession();
            sess.execCommand(cmd);
            StringBuffer buf = new StringBuffer();
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                buf.append(line).append("\n");
            }
            return buf.toString().trim();
        } finally {
            if (sess != null) {
                sess.close();
            }
        }
    }

    public static SSHUtil getInstance() {
        return sshUtil;
    }

    public boolean isLogined() {
        return isLogined;
    }

    public void closeConnection() {
        isLogined = false;
        conn.close();
    }

    public Connection getConn() {
        return conn;
    }
}