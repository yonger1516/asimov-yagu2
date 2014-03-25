package com.seven.asimov.it.utils;

import android.content.Context;
import com.seven.asimov.it.asserts.CATFAssert;
import com.seven.asimov.it.base.*;
import com.seven.asimov.it.base.constants.TFConstantsIF;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static com.seven.asimov.it.base.constants.BaseConstantsIF.*;

public final class StreamUtil {

    private StreamUtil() {
    }

    private static final int STANDARD_SLEEP_TIME = 5 * 1000;
    private static final int AVERAGE_OVERHEAD = 930;
    private static final int MS_IN_SECOND = 1000;

    private static final int TEST_SERVER_PORT = 8088;
    private static final int DEFAULT_HTTP_SERVER_PORT = 8080;


    /**
     * @param uri          -
     * @param time         - the lifetime of the current socket
     * @param trafficSpeed - bytes per second   (minimum 250 bytes per second)
     * @param bypassProxy  - Destination port = 8099 in case bypassProxy = true  otherwise destination port = 80
     */
    public static void getStream(String uri, double trafficSpeed, int time, boolean bypassProxy) {
        Socket socket = null;

        DataOutputStream dos = null;
        InputStream in = null;
        ByteArrayOutputStream baos;

        int sizeBody = (int) (STANDARD_SLEEP_TIME * trafficSpeed / MS_IN_SECOND - AVERAGE_OVERHEAD);
        if (sizeBody <= 0)
            throw new IllegalArgumentException("Incorrect traffic speed.");
        int requestNumberStream = 0;

        try {

            SocketAddress sockaddr = new InetSocketAddress(AsimovTestCase.TEST_RESOURCE_HOST, bypassProxy ? 8099 : 80);
            socket = new Socket();
            socket.setReceiveBufferSize(DEFAULT_READ_BUFFER);
            socket.setSoTimeout(TIMEOUT);
            socket.setKeepAlive(true);
            socket.connect(sockaddr, TIMEOUT);

            dos = new DataOutputStream(socket.getOutputStream());

            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < time * MS_IN_SECOND) {

                long startRequest = System.currentTimeMillis();

                final String modified = "Accept-Ranges: bytes\r\nContent-ranges: "
                        + Integer.toString(requestNumberStream) + "-" + Integer.toString(sizeBody);
                String encoded = URLEncoder.encode(Base64.encodeToString(modified.getBytes(), Base64.DEFAULT));

                HttpRequest request = AsimovTestCase.createRequest()
                        .setUri(uri)
                        .setMethod(HttpGet.METHOD_NAME)
                        .addHeaderField("X-OC-ContentEncoding", "identity")
                        .addHeaderField("X-OC-ChangeResponseContentSize", sizeBody + ",c")
                        .addHeaderField("X-OC-AddRawHeaders", encoded)
                        .addHeaderField("Random", TestUtil.generationRandomString())
                        .getRequest();

                dos.write(request.getFullRequest(true).getBytes());
                dos.flush();

                in = socket.getInputStream();
                baos = new ByteArrayOutputStream();


                int len;

                int headersLen = -1;
                int totalReceivedSize = 0;
                byte[] buffer = new byte[DEFAULT_READ_BUFFER];
                int contentLength = -1;
                List<HttpHeaderField> responseHeaders = null;
                boolean processedHeaders = false;

                try {
                    while ((len = in.read(buffer)) >= 0) {
                        totalReceivedSize += len;

                        if ((headersLen == -1) || (baos.size() < MAX_BODY_SIZE)) {
                            baos.write(buffer, 0, len);
                        }

                        if (headersLen == -1) {
                            headersLen = AsimovTestCase.checkBody(buffer);
                        }
                        if (headersLen != -1) {
                            int receivedBodyLen = totalReceivedSize - headersLen - 4 /* CRLFCRLF */;
                            if (responseHeaders == null && !processedHeaders) {
                                processedHeaders = true;
                                responseHeaders = AsimovTestCase.extractHeaderFields(baos.toByteArray());
                                for (HttpHeaderField field : responseHeaders) {
                                    if (field.getName().equalsIgnoreCase("Content-Length")) {
                                        try {
                                            contentLength = Integer.parseInt(field.getValue());
                                        } catch (NumberFormatException ignored) {

                                        }
                                    }
                                }
                            }
                            if ((contentLength != -1) && (contentLength == receivedBodyLen)) {
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Size of response for stream calculated by test application: " + totalReceivedSize);

                HttpResponse response = AsimovTestCase.buildResponse(baos);
                response.setDuration(System.currentTimeMillis() - start);
                response.setStartTime(start);

                response.setSocketInfo(new NetStat.SocketInfo(socket.getLocalAddress().getHostAddress(), socket
                        .getLocalPort(), socket.getInetAddress().getHostAddress(), socket.getPort()));

                long endResponse = System.currentTimeMillis();
                CATFAssert.assertEquals("StatusCode in stream  ", HttpStatus.SC_OK, response.getStatusCode());

                System.out.println("Time transaction : " + (endResponse - startRequest));
                TestUtil.sleep(STANDARD_SLEEP_TIME - (endResponse - startRequest));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.safeClose(in);
            IOUtil.safeClose(dos);
            IOUtil.safeClose(socket);
        }
    }

    public static void getStreamTcp(int trafficSpeed, final int time, Context context) {

        addRule(context);
        dropPriorChainIfExists();

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleServer server = new SimpleServer();
                server.run();
            }
        });
        serverThread.start();
        TestUtil.sleep(MS_IN_SECOND);

        SimpleTcpClient client = new SimpleTcpClient(trafficSpeed, time);
        client.run();
    }

    private static void addRule(Context context) {
        boolean pass = false;
        try {
            List<String> command = new ArrayList<String>();
            String uid = Integer.toString(OCUtil.getAsimovUid(context));

            command.add(TFConstantsIF.IPTABLES_PATH + " -t nat -A Z7BASECHAIN -p 6 --dport " + TEST_SERVER_PORT
                    + " -m owner \\! --uid-owner " + uid + " -j REDIRECT --to-ports " + DEFAULT_HTTP_SERVER_PORT);
            ShellUtil.execWithCompleteResult(command, true);
            pass = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed add chain to iptables");
        } finally {
            CATFAssert.assertTrue("Failed to route dns traffic through OC", pass);
        }
    }

    private static void dropPriorChainIfExists() {
        try {
            List<String> command = new ArrayList<String>();
            command.add("iptables -t nat -L OUTPUT");
            String result = ShellUtil.execWithCompleteResult(command, true);
            if (result.contains("Z7BASECHAIN-prior")) {
                command.clear();
                command.add("iptables -t nat -D OUTPUT 1");
                ShellUtil.execWithCompleteResult(command, true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed add chain to iptables");
        } finally {
            System.out.println("Rule removed.");
        }
    }

    public static class SimpleTcpClient {

        private Socket requestSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String message;

        /**
         * Traffic spedd in bytes per second
         */
        private int trafficSpeed;

        /**
         * Time when socket is opened
         */
        private int time;

        public SimpleTcpClient(int trafficSpeed, int time) {
            this.trafficSpeed = trafficSpeed;
            this.time = time;
        }

        void run() {
            try {
                //1. creating a socket to connect to the server
                requestSocket = new Socket("localhost", TEST_SERVER_PORT);
                System.out.println("Connected to localhost in port " + TEST_SERVER_PORT);
                //2. get Input and Output streams
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(requestSocket.getInputStream());
                //3: Communicating with the server
                do {
                    try {
                        message = (String) in.readObject();
                        System.out.println("server>" + message);
                        sendMessage("Hi my server");

                        long startTime = System.currentTimeMillis();
                        int countRemoveRule = 0;
                        while (System.currentTimeMillis() - startTime < time * MS_IN_SECOND) {
                            long start = System.currentTimeMillis();
                            if ((++countRemoveRule / 10.0) == (countRemoveRule / 10))
                                dropPriorChainIfExists();
                            sendMessage(TestUtil.generationRandomString(trafficSpeed));

                            TestUtil.sleep(MS_IN_SECOND - (System.currentTimeMillis() - start));
                        }
                        message = "bye";
                        sendMessage(message);
                    } catch (ClassNotFoundException classNot) {
                        System.err.println("data received in unknown format");
                    }
                } while (!message.equals("bye"));
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                //4: Closing connection
                IOUtil.safeClose(in);
                IOUtil.safeClose(out);
                IOUtil.safeClose(requestSocket);
            }
        }

        void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class SimpleServer {

        private ServerSocket providerSocket;
        private Socket connection = null;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String message;

        public SimpleServer() {

        }

        void run() {
            try {
                //1. creating a server socket
                providerSocket = new ServerSocket(TEST_SERVER_PORT, 10);
                //2. Wait for connection
                System.out.println("Waiting for connection");
                connection = providerSocket.accept();
                System.out.println("Connection received from " + connection.getInetAddress().getHostName());
                //3. get Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                sendMessage("Connection successful");
                //4. The two parts communicate via the input and output streams
                do {
                    try {
                        message = (String) in.readObject();
                        //System.out.println("client>" + message);
                        if (message.equals("bye"))
                            sendMessage("bye");
                    } catch (ClassNotFoundException classnot) {
                        System.err.println("Data received in unknown format");
                    }
                } while (!message.equals("bye"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                //4: Closing connection
                IOUtil.safeClose(in);
                IOUtil.safeClose(out);
                IOUtil.safeClose(providerSocket);
            }
        }

        void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
