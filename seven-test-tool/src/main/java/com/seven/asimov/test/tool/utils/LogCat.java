package com.seven.asimov.test.tool.utils;

import com.seven.asimov.test.tool.constants.Constants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * @author mselivanov
 */
public class LogCat {

    private static final Logger LOG = LoggerFactory.getLogger(LogCat.class.getSimpleName());

    private String mProcessPid;

    /**
     * ProcessName = null or empty, means log all.
     */
    private final String mProcessName;

    public LogCat(String processName) {
        mProcessName = processName;
    }

    public String getProcessName() {
        return mProcessName;
    }

    private OutputStream mFos = null;

    public OutputStream getFos() {
        return mFos;
    }

    public void setFos(OutputStream fos) {
        mFos = fos;
    }

    private boolean mLogLine;

    public void log(String line) {

        try {

            mLogLine = false;

            if (StringUtils.isEmpty(mProcessName)) {
                mLogLine = true;
            }

            // Log by pid
            if (!mLogLine) {

                int indexOpeningRoundBrackets = line.indexOf("(");
                int indexClosingRoundBrackets = line.indexOf(")");
                if (indexOpeningRoundBrackets != -1 && indexClosingRoundBrackets != -1
                        && (indexClosingRoundBrackets > (indexOpeningRoundBrackets + 1))) {

                    String pidFromLogCat = line.substring(indexOpeningRoundBrackets + 1, indexClosingRoundBrackets)
                            .replaceAll("\\s", StringUtils.EMPTY);

                    mProcessPid = Z7ShellUtil.checkActiveProcessByName(mProcessName);

                    // Refresh active processes if needed
                    if (StringUtils.isEmpty(mProcessPid) && !Z7ShellUtil.checkIfPidIsActive(pidFromLogCat)) {
                        Z7ShellUtil.refreshActiveProcesses();
                        mProcessPid = Z7ShellUtil.checkActiveProcessByName(mProcessName);
                    }

                    if (StringUtils.isEmpty(mProcessPid)) {
                        // LOG.w("Could not find process pid for process=%s", mProcessName);
                        return;
                    } else if (mProcessPid.equals(pidFromLogCat)) {
                        mLogLine = true;
                    }

                    // Check if we need to reset mProcessPid in some cases
                    if (mProcessName.equals("com.seven.asimov")) {
                        String valueFromLogCat = line.substring(indexClosingRoundBrackets + 3);
                        if (valueFromLogCat
                                .equals("Pub com.seven.provider.asimov: com.seven.asimov.provider.Z7ContentProvider")) {

                            Z7ShellUtil.removeActiveProcess("com.seven.asimov");

                        }
                    }
                }
            }

            if (mLogLine) {
                mFos.write(line.getBytes("UTF-8"));
                mFos.write(Constants.HTTP_NEW_LINE.getBytes("UTF-8"));
            }

        } catch (Exception e) {
            LOG.error(e.toString());
        }

    }
}
