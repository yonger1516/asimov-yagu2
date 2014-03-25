package com.seven.asimov.it.utils.conn;

import com.seven.asimov.it.utils.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class SocketsMonitor {
    private final static String TAG = "SocketsMonitor";
    private final static int OC_HTTP_PROXY_PORT = 8080;

    private int mHostPort, mOCUID;
    private List<SysSocketDescriptor> mInitialSockets;
    private List<SysSocketDescriptor> mTotalNewHostSockets;

    public SocketsMonitor(int hostPort, int OCUID) {
        mHostPort = hostPort;
        mOCUID = OCUID;

        mInitialSockets = getCurrentOCSockets();
        mTotalNewHostSockets = new ArrayList<SysSocketDescriptor>();
    }

    public void saveCurrentHostSockets() {
        ConnLogger.debug(TAG, ": saveCurrentHostSockets");

        List<SysSocketDescriptor> currentSockets = getCurrentNewSockets(false, true);
        for (SysSocketDescriptor socketDes : currentSockets) {
            if (!mTotalNewHostSockets.contains(socketDes)) {
                mTotalNewHostSockets.add(socketDes);
            }
        }
    }

    public List<SysSocketDescriptor> getTotalNewHostSockets() {
        return mTotalNewHostSockets;
    }

    public List<SysSocketDescriptor> getCurrentNewSockets() {
        return getCurrentNewSockets(true, true);
    }

    public List<SysSocketDescriptor> getCurrentLeakedHostSockets() {
        List<SysSocketDescriptor> currentSockets = getCurrentNewSockets(false, true);
        List<SysSocketDescriptor> leakedSockets = new ArrayList<SysSocketDescriptor>();
        for (SysSocketDescriptor socket : currentSockets) {
            if (socket.isLeaked()) {
                leakedSockets.add(socket);
            }
        }
        return leakedSockets;
    }

    public List<SysSocketDescriptor> getCurrentNewAppSockets() {
        return getCurrentNewSockets(true, false);
    }

    public String getCurrentNewSocketsDescription() {
        return "Current open sockets:\n" + getSocketsDescription(getCurrentNewSockets());
    }

    public String getSocketsDescription(List<SysSocketDescriptor> socketList) {
        if (socketList == null || socketList.isEmpty()) {
            return "NONE";
        }

        StringBuffer leakedSockets = new StringBuffer();
        for (SysSocketDescriptor socketDes : socketList) {
            leakedSockets.append("    Socket[srcport=");
            leakedSockets.append(socketDes.mSrcPort);
            leakedSockets.append(", destport=");
            leakedSockets.append(socketDes.mDstPort);
            leakedSockets.append(", status=");
            leakedSockets.append(socketDes.mStatus);
            leakedSockets.append("] \n");
        }

        return leakedSockets.toString();
    }

    public String dumpSocketsStatus() {
        StringBuffer status = new StringBuffer();
        status.append(dumpSocketsStatus("netstat -a", "********* NETSTAT ***********")).append("\n");
        status.append(dumpSocketsStatus(new String[]{"cat", "/proc/net/tcp"}, "********* /proc/net/tcp ***********")).append("\n");
        status.append(dumpSocketsStatus(new String[]{"cat", "/proc/net/tcp6"}, "********* /proc/net/tcp6 ***********")).append("\n");

        return status.toString();
    }

    private String dumpSocketsStatus(Object command, String prefix) {
        String separator = System.getProperty("line.separator");
        BufferedReader reader = null;
        Process aProcess;
        try {
            if (command instanceof String[]) {
                aProcess = Runtime.getRuntime().exec((String[]) command);
            } else {
                aProcess = Runtime.getRuntime().exec((String) command);
            }
            reader = new BufferedReader(new InputStreamReader(aProcess.getInputStream()));
            String line;
            StringBuilder log = new StringBuilder(prefix).append(separator);
            while ((line = reader.readLine()) != null) {
                log.append(line).append(separator);
            }
            return log.toString();
        } catch (IOException e) {
            return ": Failed to get :" + command + "output " + e;
        } finally {
            IOUtil.safeClose(reader);
        }
    }

    private List<SysSocketDescriptor> getCurrentNewSockets(boolean isIncludedAppSocket, boolean isIncludedHostSocket) {
        List<SysSocketDescriptor> newSockets = new ArrayList<SysSocketDescriptor>();
        List<SysSocketDescriptor> currentSockets = getCurrentOCSockets();
        for (SysSocketDescriptor socketDes : currentSockets) {
            if (mInitialSockets.contains(socketDes)) {
                continue;
            }

            if ((isIncludedAppSocket && OC_HTTP_PROXY_PORT == socketDes.mDstPort)
                    || (isIncludedHostSocket && mHostPort == socketDes.mDstPort)) {
                newSockets.add(socketDes);
            }
        }

        return newSockets;
    }

    private List<SysSocketDescriptor> getCurrentOCSockets() {
        ArrayList<SysSocketDescriptor> allList = ConnUtils.getSysOpenSockets();
        List<SysSocketDescriptor> hostSockets = new ArrayList<SysSocketDescriptor>();

        //have not considered port reused case.
        for (SysSocketDescriptor socketDes : allList) {
            if (socketDes.mUserID != mOCUID) {
                continue;
            }

            hostSockets.add(socketDes);
        }

        return hostSockets;
    }

}
