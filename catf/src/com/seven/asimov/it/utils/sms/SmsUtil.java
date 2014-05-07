package com.seven.asimov.it.utils.sms;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import com.seven.asimov.it.utils.PropertyLoadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

public class SmsUtil {
    private static final Logger logger = LoggerFactory.getLogger(SmsUtil.class.getSimpleName());
    private static final String PDU_BASIC_DATA = "07914151551512F2000B916105551511F100006060605130308A";
    private static final String FOOTER = "//OC";
    private static final String HEADER = "//CO";
    private static final String ACTION_SMS = "android.provider.Telephony.SMS_RECEIVED";
    private static final String FLAG_PDUS = "pdus";
    private static final byte TRIGGER_COMMAND_ID = 2; //2 - means 7tp packet
    private static final long ADDRESS_INFO = 25769803793l; //just random address

    private static final int ORIGINATOR_PACKAGE_ID = 77;    //random package id

    private static final short PCF_CONTENT_ID = 0x0128;
    private static final short PCF_COMMAND_ID = 1;

    private static final short PMS_CONTENT_ID = 0x0123;
    private static final short PMS_COMMAND_ID = 3;

    private static final short INV_CONTENT_ID = 0x0120;
    private static final short INV_COMMAND_ID = 0x03EE;

    private static final short INV_NOTIFY_TYPE = 0;
    private static final short INV_W_CACHE_SIZE = 8;  //byte(type)+byte(size)+int(subscriptionID)+byte(invType)+byte(sizeFactor)
    private static final short INV_WO_CACHE_SIZE = 7; //byte(type)+byte(size)+int(subscriptionID)+byte(invType)

    private Context ctx = null;

    public enum InvalidationType {
        //available types (from 7TP)
        INVALIDATE_WITH_CACHE((byte) 1),
        INVALIDATE_WITHOUT_CACHE((byte) 2);
        public byte byteVal;

        InvalidationType(byte b) {
            byteVal = b;
        }
    }


    /**
     * Returns new SmsUtil instance.
     *
     * @param ctx - Context is needed to broadcast Intents.
     */

    public SmsUtil(Context ctx) {
        this.ctx = ctx;
    }


    /**
     * Sends invalidate With or Without cache SMS message.
     *
     * @param subscriptionId   - defines Subscription ID (it is NOT an RR id)
     * @param invalidationType - Invalidation type is located in "InvalidationType" enum
     */
    public void sendInvalidationSms(int subscriptionId, byte invalidationType) {
        logger.info("Sending invalidation sms.");
        String userData = createMessageBody(createSubscriptionNotification(subscriptionId, invalidationType));
        sendSMS(userData);
    }

    public static void sendInvalidationSms(Context context, int subscriptionId, byte invalidationType) {
        (new SmsUtil(context)).sendInvalidationSms(subscriptionId, invalidationType);
    }

    /**
     * Sends policy update notification to OC.
     *
     * @throws java.io.IOException
     */
    public void sendPolicyUpdate(byte important) throws IOException {
        logger.info("Sending policy update.");
        String userData = createMessageBody(createPMSUpdateNotification(important));
        sendSMS(userData);
    }

    public static void sendPolicyUpdate(Context context, byte important) throws IOException {
        (new SmsUtil(context)).sendPolicyUpdate(important);
    }

    /**
     * Sends PCF policy update notification to OC.
     *
     * @throws java.io.IOException
     */
    public void sendPCFUpdate(byte important) throws IOException {
        logger.info("Sending policy update.");
        String userData = createMessageBody(createPCFUpdateNotification(important, uuidForDate(new Date())));
        sendSMS(userData);
    }

    public static void sendPCFUpdate(Context context, byte important) throws IOException {
        (new SmsUtil(context)).sendPCFUpdate(important);
    }


    /**
     * Creates uuid for current date and returns it as byte array
     *
     * @param d Date to create UUID for
     * @return byte array containing UUID bytes
     */
    private static byte[] uuidForDate(Date d) {
        final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
        long origTime = d.getTime();
        long time = origTime * 10000 + NUM_100NS_INTERVALS_SINCE_UUID_EPOCH;
        long timeLow = time & 0xffffffffL;
        long timeMid = time & 0xffff00000000L;
        long timeHi = time & 0xfff000000000000L;
        long upperLong = (timeLow << 32) | (timeMid >> 16) | (1 << 12) | (timeHi >> 48);
        UUID uuid = new UUID(upperLong, 0xC000000000000000L);

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }


    /**
     * Converts sms body to hex, adds sms header and sends sms
     *
     * @param userData Sms body.
     */
    private void sendSMS(String userData) {
        logger.info(String.format("Message was created with following body: %s", userData));
        String messagePdu = PduUtil.stringToPdu(userData);
        logger.info(String.format("PDU message: %s", messagePdu));
        String messageLength = Integer.toHexString(userData.length());
        logger.info(String.format("Message length: %s", messageLength));
        messagePdu = PDU_BASIC_DATA + messageLength + messagePdu;
        broadcastSms(messagePdu);
    }

    private void broadcastSms(String pdu) {
        logger.info("Broadcasting sms.");
        Bundle bundle = new Bundle();
        bundle.putSerializable(FLAG_PDUS, new Object[]{HexDump.hexStringToByteArray(pdu)});
        Intent intent = new Intent();
        intent.setAction(ACTION_SMS);
        intent.putExtras(bundle);
        ctx.sendBroadcast(intent);
        logger.info("Broadcasting ended.");
    }

    private String createMessageBody(String str) {
        StringBuilder bldr = new StringBuilder();
        bldr.append(FOOTER);
        bldr.append(str);
        bldr.append(HEADER);
        return bldr.toString();
    }


    private String createPMSUpdateNotification(byte important) {
        String result = null;
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteArrayStream);
        try {
            putCommonHeader(stream, PMS_CONTENT_ID, PMS_COMMAND_ID);
            putPolicyNotificationBody(stream, important);
            result = notificationBytesToSms(byteArrayStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String createSubscriptionNotification(int subscriptionID, byte type) {
        String result = null;
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteArrayStream);
        try {
            putCommonHeader(stream, INV_CONTENT_ID, INV_COMMAND_ID);
            putInvalidationNotification(stream, subscriptionID, (type == InvalidationType.INVALIDATE_WITH_CACHE.byteVal));
            result = notificationBytesToSms(byteArrayStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String createPCFUpdateNotification(byte important, byte[] uuid) {
        String result = null;
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(byteArrayStream);

        try {
            putCommonHeader(stream, PCF_CONTENT_ID, PCF_COMMAND_ID);
            putPCFNotificationBody(stream, important, uuid);
            result = notificationBytesToSms(byteArrayStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String notificationBytesToSms(byte[] bytes) throws IOException {
        String result;
        SmsNotification sms = new SmsNotification(TRIGGER_COMMAND_ID, ADDRESS_INFO, bytes, ORIGINATOR_PACKAGE_ID);
        byte[] bts = sms.encode();
        result = Base64.encodeToString(bts, Base64.DEFAULT);
        //by default. base64.encodeToString adds a \n symbol. Removing it to make it work properly.
        return result.substring(0, result.length() - 1);
    }

    private void putCommonHeader(DataOutputStream stream, short contentID, short commandID) throws IOException {
        stream.writeByte(0); //codecChainLen=0
        stream.writeShort(contentID);
        stream.writeShort(commandID);
    }


    private void putPolicyNotificationBody(DataOutputStream stream, byte important) throws IOException {
        stream.writeByte(important);
    }

    private void putPCFNotificationBody(DataOutputStream stream, byte important, byte[] uuid) throws IOException {
        stream.writeByte(important);
        stream.write(uuid);
    }

    private void putInvalidationNotification(DataOutputStream stream, int subscriptionID, boolean isIWC) throws IOException {
        stream.writeByte(INV_NOTIFY_TYPE);//Notification type

        //compatible with oc client 2.3.1 skyline
        if (PropertyLoadUtil.ocVersion.equals("2.3.1")) {
            logger.trace("oc version is " + PropertyLoadUtil.ocVersion + " enable compatible message");
            stream.writeByte(isIWC ? INV_W_CACHE_SIZE - 2 : INV_WO_CACHE_SIZE - 2);   //byte length from int to short
            stream.writeShort((short) subscriptionID);
        } else {
            stream.writeByte(isIWC ? INV_W_CACHE_SIZE : INV_WO_CACHE_SIZE);
            stream.writeInt(subscriptionID);
        }

        stream.writeByte(isIWC ? InvalidationType.INVALIDATE_WITH_CACHE.byteVal : InvalidationType.INVALIDATE_WITHOUT_CACHE.byteVal);

        if (isIWC)
            stream.writeByte(1);    //size factor
    }

    private class SmsNotification {
        byte triggerCommandId;
        long addressInfo;
        byte[] data;
        int originatorPackageId;

        public SmsNotification(byte triggerCommandId, long addressInfo, byte[] data, int originatorPackageId) {
            this.triggerCommandId = triggerCommandId;
            this.addressInfo = addressInfo;
            this.data = data;
            this.originatorPackageId = originatorPackageId;
        }

        public SmsNotification(String body) throws IOException {
            byte[] data;
            try {
                data = Base64.decode(body, Base64.DEFAULT);
            } catch (IllegalArgumentException e) {
                throw new IOException(e.getMessage());
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream in = new DataInputStream(bais);

            this.triggerCommandId = in.readByte();
            this.addressInfo = in.readLong();
            byte appDataSize = in.readByte();
            byte[] appData = new byte[appDataSize];
            int bytesRead = in.read(appData);
            if (bytesRead != appDataSize) {
                throw new IOException("Could not read expected " + appDataSize + " bytes for application data");
            }
            this.originatorPackageId = in.readInt();
        }

        public int getOriginatorPackageId() {
            return originatorPackageId;
        }

        public void setOriginatorPackageId(int originatorPackageId) {
            this.originatorPackageId = originatorPackageId;
        }

        public byte getTriggerCommandId() {
            return triggerCommandId;
        }

        public void setTriggerCommandId(byte triggerCommandId) {
            this.triggerCommandId = triggerCommandId;
        }

        public long getAddressInfo() {
            return addressInfo;
        }

        public void setAddressInfo(long addressInfo) {
            this.addressInfo = addressInfo;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public byte[] encode() throws IOException {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            DataOutputStream dStrm = new DataOutputStream(result);
            dStrm.writeByte(triggerCommandId);
            dStrm.writeLong(addressInfo);
            dStrm.writeByte(data.length);
            if (data != null) {
                dStrm.write(data);
            }
            dStrm.writeInt(originatorPackageId);
            return result.toByteArray();
        }
    }
}


