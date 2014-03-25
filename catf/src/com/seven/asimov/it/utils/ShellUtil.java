package com.seven.asimov.it.utils;

import android.util.Log;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class ShellUtil {

    private static final Logger logger = LoggerFactory.getLogger(ShellUtil.class.getSimpleName());

    /**
     * Executes command and returns (stdout + newline + stderr) as result
     *
     * @param command trimmed command
     * @return execution result
     */
    public static String execWithCompleteResult(List<String> command, boolean asSuperuser) {
        String result = null;
        Process aProcess = null;
        try {
            if (asSuperuser) {
                command = addSuParams(command);
            }

            logger.debug("Executing: " + convertToString(command));

            String[] cmdArr = new String[command.size()];
            command.toArray(cmdArr);
            aProcess = Runtime.getRuntime().exec(cmdArr);

            String stderr = getStreamData(aProcess.getErrorStream());
            String stdout = getStreamData(aProcess.getInputStream());
            result = stdout + "\n" + stderr;

            int exitValue = aProcess.waitFor();
            if (exitValue == 0) {
                // Success, hopefully this InetAddress makes sense.
                logger.debug("Execution result: " + result);
            } else {
                logger.debug("Unable to obtain result for: " + convertToString(command) + "\n" + result);
            }
        } catch (Exception ex) {
            logger.debug("Exception during: " + convertToString(command), ex);
        } finally {
            if (aProcess != null) {
                try {
                    aProcess.destroy();
                } catch (Exception ignored) {
                    logger.info("Good destroy!!!");
                }
            }
        }
        return result;
    }

    private static List<String> addSuParams(List<String> params) {
        ArrayList<String> rbParams = new ArrayList<String>();
        rbParams.add("su");
        rbParams.add("-c");
        rbParams.add(convertToString(params));
        params = rbParams;
        return params;
    }

    public static String getStreamData(InputStream is) {
        logger.trace("getStreamData: start");
        try {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(is), 4096);
            String line;
            StringBuilder sbuf = new StringBuilder();
            while ((line = rdr.readLine()) != null) {
                sbuf.append(line);
                sbuf.append("\n");
            }
            return sbuf.toString();
        } catch (Exception e) {
            logger.error("Error in getStreamData", e);
        }
        return "";
    }

    private static String convertToString(List<String> params) {
        StringBuilder sbuf = new StringBuilder();

        for (String value : params) {
            sbuf.append(value);
            sbuf.append(" ");
        }
        sbuf.setLength(sbuf.length() - 1);

        return sbuf.toString();
    }

    public static String execSimple(String command) {
        String result = null;
        Process process;

        try {
            process = Runtime.getRuntime().exec(command);
            String stderr = getStreamData(process.getErrorStream());
            String stdout = getStreamData(process.getInputStream());
            result = stdout + "\n" + stderr;
        } catch (IOException e) {
            logger.error("Not executed! Message: " + e.getMessage());
        }
        return result;
    }

    public static void kill(int PID) {
        kill(PID, 15);
    }

    public static void kill(int PID, int signal) {
        logger.info("Killing PID: " + PID + " with signal: " + signal);
        try {
            if (PID > 0)
                Runtime.getRuntime().exec(new String[]{"su", "-c", "kill -" + signal + " " + PID}).waitFor();
        } catch (IOException e) {
            Log.e("Shell", "Not killed!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void killAll(String processName) {
        killAll(processName, 15);
    }

    public static void killAll(String processName, int signal) {
        String[] psOutput = execSimple("ps").split("\\n+");
        for (String entry : psOutput) {
            if (entry.endsWith(processName)) {
                System.out.println(entry);
                String[] killArray = entry.split("\\s+");
                kill(Integer.parseInt(killArray[1]), signal);
            }
        }
    }

    public static boolean rebootDevice() {

        String command = "reboot";

        logger.info("Reboot!");

        return execShellSuCommand(command);

    }

    public static boolean execShellSuCommand(String command) {
        DataOutputStream os = null;
        try {

            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();

            return true;

        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;

        } finally {
            IOUtil.safeClose(os);

        }

    }

    public static void killProcesses(List<Integer> killPids) throws IOException, InterruptedException {
        for (int pid : killPids) {
            kill(pid, 9);
        }
    }

    public static List<String> execWithCompleteResultWithListOutput(List<String> command, boolean asSuperuser) {
        List<String> result = null;
        Process aProcess = null;
        try {
            if (asSuperuser) {
                command = addSuParams(command);
            }

            logger.debug("Executing: " + convertToString(command));

            String[] cmdArr = new String[command.size()];
            command.toArray(cmdArr);
            aProcess = Runtime.getRuntime().exec(cmdArr);

            List<String> stdout = getStreamDataAsArray(aProcess.getInputStream());
            result = stdout;

            int exitValue = aProcess.waitFor();
            if (exitValue == 0) {
                // Success, hopefully this InetAddress makes sense.
                logger.debug("Execution result: " + result);
            } else {
                logger.debug("Unable to obtain result for: " + convertToString(command) + "\n" + result);
            }
        } catch (Exception ex) {
            logger.debug("Exception during: " + convertToString(command), ex);
        } finally {
            if (aProcess != null) {
                try {
                    aProcess.destroy();
                } catch (Exception ignored) {
                    logger.info("Good destroy!!!");
                }
            }
        }
        return result;
    }

    private static List<String> getStreamDataAsArray(InputStream is) {
        try {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(is), 4096);
            String line;
            List<String> list = new ArrayList<String>();
            while ((line = rdr.readLine()) != null) {
                list.add(line);
            }
            return list;
        } catch (Exception e) {
            logger.error("Error in getStreamData", e);
        }
        return null;
    }

    /**
     * Get access rights of file
     *
     * @param path     path to file
     * @param fileName file name
     * @return rights of file as "-rwxr-x---", or null if file not found for this path
     */
    public static String getAccessRightsOfFile(String path, String fileName) {
        String[] command = {"su", "-c", "ls -l " + path};
        Process process;
        String result = null;
        try {
            process = Runtime.getRuntime().exec(command);
            String stderr = getStreamData(process.getErrorStream());
            String stdout = getStreamData(process.getInputStream());
            String[] psOutput = (stdout + "\n" + stderr).split("\\n+");
            for (String entry : psOutput) {
                if (entry.contains(fileName)) {
                    result = entry.trim().substring(0, 10);
                }
            }
        } catch (IOException e) {
            logger.error("Not executed! Message: " + e.getMessage());
        }
        return result;
    }

    public static void copyFile(String source, String destination) {
        try {
            String[] permission = {"su", "-c", "cat " + source + " > " + destination};
            Runtime.getRuntime().exec(permission).waitFor();
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public static void removeDirectory(String path) {
        try {
            String[] permission = {"su", "-c", "rm -r ", path};
            Runtime.getRuntime().exec(permission).waitFor();
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        } catch (InterruptedException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
