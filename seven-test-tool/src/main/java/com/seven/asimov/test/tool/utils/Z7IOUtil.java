package com.seven.asimov.test.tool.utils;

import com.seven.asimov.test.tool.core.TestFactoryOptions;
import com.seven.asimov.test.tool.receivers.MyPhoneStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Z7IOUtil {
    private static final Logger LOG = LoggerFactory.getLogger(Z7IOUtil.class.getSimpleName());

    public static void transferFileToServer(String pathToFile) {

        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String fileName = pathToFile.substring(pathToFile.lastIndexOf("/") + 1);

        String urlServer = "http://" + TestFactoryOptions.getsTestSuiteHost() + ":8099" + "/uploadLog?logFile="
                + fileName + "&msisdn=" + MyPhoneStateListener.getDeviceId();
        // String twoHyphens = "--";
        // String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathToFile));

            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Enable POST method
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            // connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            // outputStream.writeBytes(twoHyphens + boundary + Constants.HTTP_NEW_LINE);
            // outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToFile
            // + "\"" + Constants.HTTP_NEW_LINE);
            // outputStream.writeBytes(Constants.HTTP_NEW_LINE);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // outputStream.writeBytes(Constants.HTTP_NEW_LINE);
            // outputStream.writeBytes(twoHyphens + boundary + twoHyphens + Constants.HTTP_NEW_LINE);

            // Here server begins handle request
            // Responses from the server (code and message)

            if (connection.getResponseCode() == 200) {
                LOG.info("Log " + fileName + " uploaded to -> " + url.getHost());
            } else {
                LOG.warn("Failed to upload log " + fileName + " to -> " + url.getHost());
            }

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}
