package com.seven.asimov.it.utils.conn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.StringTokenizer;

import android.util.Log;
import com.seven.asimov.it.base.HttpHeaderField;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import com.seven.asimov.it.utils.ShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.seven.asimov.it.base.constants.BaseConstantsIF.*;


public final class ConnUtils {
    private static final Logger logger = LoggerFactory.getLogger(ConnUtils.class.getSimpleName());
    private static final int	  STR_NOT_FOUND = -1;
    
    /**
     * Hidden constructor.
     */
    private ConnUtils() {
        // Empty
    }

    private static CharsetEncoder encoder;
    private static CharsetDecoder decoder;

    static {
        Charset charset = Charset.forName("ISO-8859-1");
        encoder = charset.newEncoder();
        decoder = charset.newDecoder();
    }

    public static ByteBuffer stringToByteBuffer(String msg){
        try{
            return encoder.encode(CharBuffer.wrap(msg));
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

    public static String ByteBufferToString(ByteBuffer buffer){
        decoder.reset();

        String data;
        try{
            int old_position = buffer.position();
            data = decoder.decode(buffer).toString();
            // reset buffer's position to its original so it is not altered:
            buffer.position(old_position);  
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
        return data;
    }

    public static void encode(String message, ByteBuffer buffer){
        encoder.reset();
        encoder.encode(CharBuffer.wrap(message), buffer, true);
        encoder.flush(buffer);
    }

    public static ByteBuffer encode(String message) throws CharacterCodingException{
        return encoder.encode(CharBuffer.wrap(message));
    }

    public static InetAddress getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        logger.info("LocaloopAddress=" + inetAddress.getHostAddress());
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("getLocalIpAddress failed", e);
        }
        
        return null;
    }

    /**
     * TODO: simple HTTP request parser - assume buffer contains a completed request
     * @param buffer byte buffer
     * @return HTTP request
     */
    public static HttpRequest parseHttpRequest(ByteBuffer buffer) {
        String request = ByteBufferToString(buffer);
        if (request == null) {
        	return null;
        }
        
        int index1 = request.indexOf(CRLF);
        int index2 = request.indexOf(CRLF + CRLF, index1);
        if (index1 == -1 || index2 == -1) {
            return null;
        }
        
        String requestLine = request.substring(0, index1);
        String headers = request.substring(index1+2, index2);

        int indexSP1 = requestLine.indexOf(" ");
        int indexSP2 = requestLine.indexOf(" ", indexSP1 + 1);
        if (indexSP1 == -1 || indexSP2 == -1) {
        	return null;
        }
        
        int start = buffer.position();
        HttpRequest httpRequest = new HttpRequest();

        // set method and uri
        httpRequest.setMethod(requestLine.substring(0, indexSP1));
        httpRequest.setUri(requestLine.substring(indexSP1+1, indexSP2));
        
        int bodySize = 0;
        StringTokenizer st = new StringTokenizer(headers, CRLF);
        while (st.hasMoreTokens()) {
            String header = st.nextToken();
            int i = header.indexOf(": ");
            if (i == -1) continue;
            String key = header.substring(0, i);
            if (key.equals("Content-Length")) {
                bodySize = Integer.parseInt(header.substring(i+2)); 
            }
            httpRequest.addHeaderField(new HttpHeaderField(key, header.substring(i+2)));
        }

        if (bodySize > 0 && request.length() < index2+4+bodySize) {
        	return null;
        }

        // set body
        if (bodySize > 0) {
            httpRequest.setBody(request.substring(index2+4, index2+4+bodySize));
        }

        buffer.position(start + index2 + 4 + bodySize);

        return httpRequest;
    }
    
    public static HttpResponse parseHttpResponse(ByteBuffer buffer) {
    	return parseHttpResponse(buffer, null);
    }
    
    /**
     * TODO: simple HTTP response parser - assume buffer contains a completed response
     * @param buffer byte buffer
     * @return HTTP response
     */
	public static HttpResponse parseHttpResponse(ByteBuffer buffer, HttpResponse origResp) {
    	
		if (origResp == null) {
			String response = ByteBufferToString(buffer);

			if (response == null) {
				return null;
			}

			int iStatusLineEnd = response.indexOf(CRLF);
			int iHeadersEnd = response.indexOf(CRLF + CRLF, iStatusLineEnd);

			if (iStatusLineEnd == STR_NOT_FOUND || iHeadersEnd == STR_NOT_FOUND) {
				return null;
			}

			int start = buffer.position();
			String statusLine = response.substring(0, iStatusLineEnd);
			String headers = response.substring(iStatusLineEnd + 1, iHeadersEnd);

			int iHTTPMethod = statusLine.indexOf(" ");
			int iRESPCode = statusLine.indexOf(" ", iHTTPMethod + 1);
			if (iHTTPMethod == STR_NOT_FOUND || iRESPCode == STR_NOT_FOUND) {
				return null;
			}

			HttpResponse httpResponse = new HttpResponse();
			httpResponse.setStatusLine(statusLine);
			httpResponse.setStatusCode(getStatusCode(statusLine));

			int bodySize = 0;
			boolean isBinaryBody = false;
			StringTokenizer st = new StringTokenizer(headers, CRLF);

			while (st.hasMoreTokens()) {
				String header = st.nextToken();
				int i = header.indexOf(": ");
				if (i == STR_NOT_FOUND)
					continue;
				String key = header.substring(0, i);
				if (key.equals("Content-Length")) {
					bodySize = Integer.parseInt(header.substring(i + 2));					
					httpResponse.setBodyLength(bodySize);
				} else if (key.equals("Content-Type")) {
					String contentType = header.substring(i + 2);
					isBinaryBody = contentType != null && contentType.equals("image/jpeg"); //TODO: Other binary type?
				    httpResponse.setBinaryBody(isBinaryBody);
				}
				httpResponse.addHeaderField(new HttpHeaderField(key, header.substring(i + 2)));
			}

			if (bodySize > 0 && response.length() < iHeadersEnd + 4 + bodySize) {
				if (isBinaryBody) {
					httpResponse.setFullBodyRecieved(false);
					httpResponse.setBodyBytesRecived(buffer.limit() - iHeadersEnd - 4);
					buffer.clear();
					httpResponse.setBody("<-OBJECT->");
					return httpResponse;
				} else
					return null;
			}

			// set body
			if (bodySize > 0) {
				httpResponse.setBody(response.substring(iHeadersEnd + 4, iHeadersEnd + 4 + bodySize));
			}

			int newPos = start + iHeadersEnd + 4 + bodySize;
			buffer.position(newPos);

			return httpResponse;
		} else {
			//Since this is leftover data, just consume buffer and clear it.
			origResp.addBodyBytesRecieved(buffer.limit());
			buffer.clear();
			if (origResp.getBodyBytesRecieved() >= origResp.getBodyLength())			{
				origResp.setFullBodyRecieved(true);
			}
			return origResp;
		}
    		
    }
    
    private static int getStatusCode(String statusLine) {
        if (statusLine == null || statusLine.length() <= 0) {
            return -1;
        }

        String[] statusElemements = statusLine.split(" ");
        int index = 0;
        int code = -1;
        for (String elemement:statusElemements) {
            if (elemement == null || elemement.length() <= 0) {
                continue;
            }

            if (index == 1) {
                try {
                    code = Integer.parseInt(elemement);
                } catch (NumberFormatException e) {
                    //ignored
                }
                break;
            }

            index ++;
        }

        return code;
    }
    
    /**
     * Obtain a list of currently open sockets on the device.
     * @return opened sockets
     */
    public static ArrayList<SysSocketDescriptor> getSysOpenSockets()
    {
        //the current method for obtaining the list of open sockets is to parse the
        // tcp and tcp6 files in the /proc/net directory.
        // note: we will want to find a more robust method of obtaining this information
        long startMillis = System.currentTimeMillis();
        ArrayList<SysSocketDescriptor> tcpSockets = parseProcNetTcpFile("/proc/net/tcp", -1, null);
        ArrayList<SysSocketDescriptor> tcp6Sockets = parseProcNetTcpFile("/proc/net/tcp6", -1, null);
        tcpSockets.addAll(tcp6Sockets);
        long delta = System.currentTimeMillis() - startMillis;
        if (delta > 0)
            delta = delta + 1;

        return tcpSockets;
    }
    
    /**
     * Called to parse a /proc/net file at the specified path. If a port number is
     * provided, then only sockets matching that port number are returned.
     * @param path to file
     * @param port Optional port number to match, or -1 for all sockets.
     * @param buf An optional buffer to use for reading socket file data. If null, then
     *              a buffer is allocated internally.
     * @return Array of SysSocketDescriptor objects.
     */
    private static ArrayList<SysSocketDescriptor> parseProcNetTcpFile(String path, int port, byte[] buf)
    {
        /**
         * the output from the tcp file looks like this:
         * sl local_address rem_address   st tx_queue:rx_queue tr:tm->when retrnsmt   uid   timeout inode
         * 0: 00000000:2606 00000000:0000 0A 00000000:00000000 00:00000000 00000000     0        0  588     1       db344040 300 0 0 2 -1
         * 1: 00000000:410A 00000000:0000 0A 00000000:00000000 00:00000000 00000000 10008        0  2047    1       db3444c0 300 0 0 2 -1
         * 2: 00000000:09AF 00000000:0000 0A 00000000:00000000 00:00000000 00000000     0        0  4082    1       c7cc7b40 300 0 0 2 -1
         */
        ArrayList<SysSocketDescriptor> openSockets = new ArrayList<SysSocketDescriptor>();

        try
        {
            if (buf == null)
                buf = new byte[4*1024];
            int fBufferLen = buf.length;
            FileInputStream  tcpFile = new FileInputStream(path);
            int lineNum = 1;
            int colIx = -1;
            boolean inColumn = false;
            final char[] value = new char[40];
            int valueLen = 0;
            SysSocketDescriptor desc = new SysSocketDescriptor();

            while (true)
            {
                int bytesRead = tcpFile.read(buf, 0, fBufferLen);
                if (bytesRead <= 0)
                    break;

                for (int ix = 0; ix < bytesRead; ix++)
                {
                    char c = (char)buf[ix];

                    //throw away the first line which is simply the column headers
                    // note: if the columns ever change we may need to parse this to find
                    // what we are looking for.
                    if (c != '\n' && lineNum == 1)
                        continue;

                    if (c == '\n')
                    {
                        //end of line - add the current descriptor to the list and start a new one
                        // if we were provided a specific port to look for and we found it, exit now
                        if (desc != null && (port < 0 || port == desc.mSrcPort))
                        {
                            openSockets.add(desc);
                            if (port > 0)
                                return openSockets;
                            desc = new SysSocketDescriptor();
                        }
                            
                        colIx = -1;
                        inColumn = false;
                        lineNum++;
                    }
                    else if (c == ' ' && inColumn)
                    {
                        //a space will end a column if we were parsing one
                        switch (colIx)
                        {
                            case 1:
                            {
                                //local_address
                                if (valueLen > 16) {
                                    //tcp6 address
                                    desc.mSrcAddress =parseIpv6formatIpadd(value); 
                                    desc.mSrcPort = Integer.parseInt(new String(value, 33, 37 - 33), 16);
                                } else {
                                    //tcp4 address
                                    desc.mSrcAddress = fromHexString(new String(value, 0, 8));
                                    desc.mSrcPort = Integer.parseInt(new String(value, 9, 13 - 9), 16);
                                }
                                break;
                            }
                            case 2:
                            {
                                //destination_address
                                if (valueLen > 16) {
                                    //tcp6 address
                                    desc.mDstAddress = parseIpv6formatIpadd(value);
                                    desc.mDstPort = Integer.parseInt(new String(value, 33, 37 - 33), 16);
                                } else {
                                    //tcp4 address
                                    desc.mDstAddress = fromHexString(new String(value, 0, 8));
                                    desc.mDstPort = Integer.parseInt(new String(value, 9, 13 - 9), 16);
                                }
                                break;
                            }
                            case 3:
                            {
                                //status
                                if(valueLen > 0) {
                                    desc.mStatus = SysSocketDescriptor.TcpStatus.getStatus(Integer.parseInt(new String(value, 0, valueLen), 16));
                                }
                                break;
                            }
                            case 7:
                            {
                                //uid
                                desc.mUserID = Integer.parseInt(new String(value, 0, valueLen));
                                break;
                            }
                        }

                        inColumn = false;
                        valueLen = 0;
                    }
                    else if (c != ' ')
                    {
                        //start a new column if we hadn't already
                        if (!inColumn)
                        {
                            inColumn = true;
                            colIx++;
                        }
                        
                        //if we are currently parsing a column we are interested in
                        // add the current char to the string builder.
                        if (colIx == 1 || colIx == 2 || colIx == 3 || colIx == 7) {
                            value[valueLen++] = c;
                        }
                    }
                }
            }
        }
        catch(FileNotFoundException e)
        {
            logger.warn("failed to open tcp file", e);
        }
        catch (IOException e)
        {
            logger.error("error reading tcp file", e);
        }
        catch (Throwable t)
        {
            logger.error("unhandled exception reading tcp file", t);
        }

        return openSockets;
    }
    
    private static byte[] parseIpv6formatIpadd(char[] val) {

        byte[] ipv6Bytearr = fromHexString(new String(val, 0, 32));
        InetAddress inetAdd;
        try {
            inetAdd = InetAddress.getByAddress(ipv6Bytearr);
            // If the obtained ip , is ipv6 compatible ipv4 , we consider only
            // the the last 4 bytes,else return ipv6 address
            if (inetAdd instanceof Inet6Address
                    && !((Inet6Address) inetAdd).isIPv4CompatibleAddress()) {
                // ip addr is obtained in this format 1.0.0.127, we need to
                // reverse and then store
                ipv6Bytearr = reverseIPByte(fromHexString(new String(val, 24, 32 - 24)));
            }
        } catch (UnknownHostException e) {

            logger.error(
                    "Unknown Host exception thrown for ipv6 format ip: "
                            + new String(val, 0, 32));
        }

        return ipv6Bytearr;
    }
    
    public static byte[] reverseIPByte(byte[] ipByteAddr) {
        for (int i = 0; i < ipByteAddr.length / 2; i++) {
            byte temp = ipByteAddr[i];
            ipByteAddr[i] = ipByteAddr[ipByteAddr.length - i - 1];
            ipByteAddr[ipByteAddr.length - i - 1] = temp;
        }

        return ipByteAddr;
    }
    
    public static byte [] fromHexString(String s) {
        if (s == null)
            return null;
        s = s.toLowerCase();
        int i, j=0, len = s.length();
        byte [] result = new byte[len/2];
        for (i=0; i<len; i+=2) {
            char chi = s.charAt(i), clo = s.charAt(i+1);
            int nibhi = (chi <= '9')? (int)(chi - '0') : (int)(chi-'a')+10;
            int niblo = (clo <= '9')? (int)(clo - '0') : (int)(clo-'a')+10;
            result[j++] = (byte)((nibhi << 4) + niblo);
        }
        return result;
    }
    
    public static boolean block7TP() {
        String[] flushCmdArr =  new String[] {"/system/bin/iptables", "-t", "filter", "-A", "OUTPUT", "-p", "tcp", "--dport", "7735", "-j", "REJECT", "--reject-with", "tcp-reset"};
        ArrayList<String> cmd = new ArrayList<String>(Arrays.asList(flushCmdArr));
        String result = ShellUtil.execWithCompleteResult(cmd, true);
        return (result != null);
    }
    
    public static boolean unblock7TP() {
        String[] flushCmdArr =  new String[] {"/system/bin/iptables", "-t", "filter", "-F"};
        ArrayList<String> cmd = new ArrayList<String>(Arrays.asList(flushCmdArr));
        String result = ShellUtil.execWithCompleteResult(cmd, true);
        return (result != null);
    }

    public static String getHostAddress(String hostName) throws UnknownHostException {
        logger.trace("getHostAddress: hostName: " + hostName);
        for (int i = 0; i < 3; i++) {
            InetAddress[] addresses = InetAddress.getAllByName(hostName);
            for (InetAddress addr : addresses) {
                if (addr instanceof Inet4Address) {
                    logger.trace("getHostAddress: HostAddress: " + addr.getHostAddress());
                    return addr.getHostAddress();
                }
            }
        }
        return null;
    }
}
