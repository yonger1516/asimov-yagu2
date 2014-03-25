package com.seven.asimov.it.utils.tcpdump;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class DbAdapter extends SQLiteOpenHelper {

    private static final Logger logger = LoggerFactory.getLogger(DbAdapter.class.getSimpleName());

    private static DbAdapter instance;

    private PacketPersistenceMode packetPersistenceMode = PacketPersistenceMode.PERSIST_DOWNSTREAM_WO_PAYLOAD;

    public static final String DATABASE_NAME = "tcpdump.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TCP_PACKETS_NAME_TABLE = "tcp_packets";
    private static final String DNS_PACKETS_NAME_TABLE = "dns_packets";

    private static final String ID_FIELD = "id";
    private static final String TIMESTAMP_FIELD = "timestamp";
    private static final String TOTAL_LENGTH_FIELD = "total_length";
    private static final String PAYLOAD_LENGTH_FIELD = "payload_length";
    private static final String INTERFACE_FIELD = "interface";
    private static final String DIRECTION_FIELD = "direction";
    private static final String SRC_ADDR_FIELD = "src_addr";
    private static final String DEST_ADDR_FIELD = "dest_addr";
    private static final String SRC_PORT_FIELD = "src_port";
    private static final String DEST_PORT_FIELD = "dest_port";
    private static final String SYN_FIELD = "syn";
    private static final String ACK_FIELD = "ack";
    private static final String PSH_FIELD = "psh";
    private static final String FIN_FIELD = "fin";
    private static final String RST_FIELD = "rst";
    private static final String SEQ_NUMBER_FIELD = "seq_number";
    private static final String ACK_NUMBER_FIELD = "ack_number";
    private static final String HOST_FIELD = "host";
    private static final String TRANSACTION_ID_FIELD = "transaction_id";
    private static final String RAW_BODY_FIELD = "raw_body";

    private static final String INTEGER_TYPE = " integer, ";
    private static final String TEXT_TYPE = " text, ";
    private static final String TYPE_FIELD = "TYPE";
    private static final String DDATA_FIELD = "DDATA";
    private static final String REPORT_TABLE_NAME = "report";
    private static final String reportDataBaseFilename = "/data/data/com.seven.asimov/reporting.db";
    private static int databaseCoefficient = 0;

    private static final String TIME_CONDITION = TIMESTAMP_FIELD + ">=? and " + TIMESTAMP_FIELD + "<=?";

    private static SQLiteDatabase dataBase;

    private final List<TcpPacket> tcpPacketsBuffer = new ArrayList<TcpPacket>();
    private static final int MAX_TCP_PACKETS_BUFFER_SIZE = 50;

    public static String getReportDataBaseFilename() {
        return reportDataBaseFilename;
    }

    public static DbAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new DbAdapter(context);
        }
        return instance;
    }

    private DbAdapter(Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private DbAdapter(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        dataBase = getWritableDatabase();
    }

    public void setPacketPersistenceMode(PacketPersistenceMode packetPersistenceMode) {
        this.packetPersistenceMode = packetPersistenceMode;
    }

    public void storeTcpPacket(TcpPacket packet, boolean useBuffer) {
        if (useBuffer) {
            synchronized (tcpPacketsBuffer) {
                tcpPacketsBuffer.add(packet);
                if (tcpPacketsBuffer.size() >= MAX_TCP_PACKETS_BUFFER_SIZE) {
                    flushTcpPackets();
                }
            }
        } else {
            storeTcpPacket(packet);
        }
    }

    public synchronized void flushTcpPackets() {
        try {
            synchronized (tcpPacketsBuffer) {
                dataBase.beginTransaction();
                String query = "Insert or Replace into tcp_packets " +
                        "(timestamp, total_length, payload_length, interface, direction, src_addr, dest_addr, " +
                        "src_port, dest_port, syn, ack, psh, fin, rst, seq_number, ack_number, raw_body) " +
                        "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                SQLiteStatement statement = dataBase.compileStatement(query);
                for (TcpPacket packet : tcpPacketsBuffer) {
                    int index = 0;
                    statement.bindLong(++index, packet.getTimestamp());
                    statement.bindLong(++index, packet.getTotalLength());
                    statement.bindLong(++index, packet.getPayloadLength());
                    statement.bindLong(++index, packet.getInterface().ordinal());
                    statement.bindLong(++index, packet.getDirection().ordinal());
                    statement.bindString(++index, packet.getSourceAddress());
                    statement.bindString(++index, packet.getDestinationAddress());
                    statement.bindLong(++index, packet.getSourcePort());
                    statement.bindLong(++index, packet.getDestinationPort());
                    statement.bindLong(++index, packet.isSyn() ? 1 : 0);
                    statement.bindLong(++index, packet.isAck() ? 1 : 0);
                    statement.bindLong(++index, packet.isPsh() ? 1 : 0);
                    statement.bindLong(++index, packet.isFin() ? 1 : 0);
                    statement.bindLong(++index, packet.isRst() ? 1 : 0);
                    statement.bindLong(++index, packet.getSequenceNumber());
                    statement.bindLong(++index, packet.getAcknowledgementNumber());
                    if ((packetPersistenceMode == PacketPersistenceMode.PERSIST_ALL
                            || (packetPersistenceMode == PacketPersistenceMode.PERSIST_DOWNSTREAM_WO_PAYLOAD
                            && packet.getDirection() == Direction.FROM_US)) && packet.getDataBytes() != null) {
                        statement.bindBlob(++index, packet.getDataBytes());
                    } else {
                        statement.bindNull(++index);
                    }
                    statement.execute();
                }
                tcpPacketsBuffer.clear();
                dataBase.setTransactionSuccessful();
            }
            ;
        } finally {
            dataBase.endTransaction();
        }
    }

    void storeTcpPacket(TcpPacket packet) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TIMESTAMP_FIELD, packet.getTimestamp());
        contentValues.put(TOTAL_LENGTH_FIELD, packet.getTotalLength());
        contentValues.put(PAYLOAD_LENGTH_FIELD, packet.getPayloadLength());
        contentValues.put(INTERFACE_FIELD, packet.getInterface().ordinal());
        contentValues.put(DIRECTION_FIELD, packet.getDirection().ordinal());
        contentValues.put(SRC_ADDR_FIELD, packet.getSourceAddress());
        contentValues.put(DEST_ADDR_FIELD, packet.getDestinationAddress());
        contentValues.put(SRC_PORT_FIELD, packet.getSourcePort());
        contentValues.put(DEST_PORT_FIELD, packet.getDestinationPort());
        contentValues.put(SYN_FIELD, packet.isSyn() ? 1 : 0);
        contentValues.put(ACK_FIELD, packet.isAck() ? 1 : 0);
        contentValues.put(PSH_FIELD, packet.isPsh() ? 1 : 0);
        contentValues.put(FIN_FIELD, packet.isFin() ? 1 : 0);
        contentValues.put(RST_FIELD, packet.isRst() ? 1 : 0);
        contentValues.put(SEQ_NUMBER_FIELD, packet.getSequenceNumber());
        contentValues.put(ACK_NUMBER_FIELD, packet.getAcknowledgementNumber());
        if (packetPersistenceMode == PacketPersistenceMode.PERSIST_ALL
                || (packetPersistenceMode == PacketPersistenceMode.PERSIST_DOWNSTREAM_WO_PAYLOAD
                && packet.getDirection() == Direction.FROM_US)) {
            contentValues.put(RAW_BODY_FIELD, packet.getDataBytes());
        }
        insertEntry(TCP_PACKETS_NAME_TABLE, contentValues);
    }

    public void storeDnsPacket(DnsPacket packet) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TIMESTAMP_FIELD, packet.getTimestamp());
        contentValues.put(TOTAL_LENGTH_FIELD, packet.getTotalLength());
        contentValues.put(PAYLOAD_LENGTH_FIELD, packet.getPayloadLength());
        contentValues.put(INTERFACE_FIELD, packet.getInterface().ordinal());
        contentValues.put(DIRECTION_FIELD, packet.getDirection().ordinal());
        contentValues.put(SRC_ADDR_FIELD, packet.getSourceAddress());
        contentValues.put(DEST_ADDR_FIELD, packet.getDestinationAddress());
        contentValues.put(SRC_PORT_FIELD, packet.getSourcePort());
        contentValues.put(DEST_PORT_FIELD, packet.getDestinationPort());
        contentValues.put(HOST_FIELD, packet.getHost());
        contentValues.put(TRANSACTION_ID_FIELD, packet.getTransactionId());
        contentValues.put(RAW_BODY_FIELD, packet.getDataBytes());
        insertEntry(DNS_PACKETS_NAME_TABLE, contentValues);
    }

    public List<TcpPacket> getTcpPackets(long startTime, long endTime, boolean bodyRequired) {
        flushTcpPackets();
        List<TcpPacket> packets = new ArrayList<TcpPacket>();
        String[] columnsIncludingBody = new String[]{TIMESTAMP_FIELD, TOTAL_LENGTH_FIELD, PAYLOAD_LENGTH_FIELD,
                INTERFACE_FIELD, DIRECTION_FIELD, SRC_ADDR_FIELD, DEST_ADDR_FIELD, SRC_PORT_FIELD, DEST_PORT_FIELD,
                SYN_FIELD, ACK_FIELD, PSH_FIELD, FIN_FIELD, RST_FIELD, SEQ_NUMBER_FIELD, ACK_NUMBER_FIELD, RAW_BODY_FIELD};
        String[] columnsExcludingBody = Arrays.copyOfRange(columnsIncludingBody, 0, columnsIncludingBody.length - 1);
        Cursor cursor = dataBase.query(
                TCP_PACKETS_NAME_TABLE,
                bodyRequired ? columnsIncludingBody : columnsExcludingBody,
                TIME_CONDITION,
                new String[]{String.valueOf(startTime), String.valueOf(endTime)},
                null, null, ID_FIELD);
        if (cursor.moveToFirst()) {
            do {
                packets.add(PacketBuilder.buildTcpPacket(
                        cursor.getLong(cursor.getColumnIndexOrThrow(TIMESTAMP_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TOTAL_LENGTH_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(PAYLOAD_LENGTH_FIELD)),
                        Interface.values()[cursor.getInt(cursor.getColumnIndexOrThrow(INTERFACE_FIELD))],
                        Direction.values()[cursor.getInt(cursor.getColumnIndexOrThrow(DIRECTION_FIELD))],
                        cursor.getString(cursor.getColumnIndexOrThrow(SRC_ADDR_FIELD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DEST_ADDR_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(SRC_PORT_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DEST_PORT_FIELD)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(SEQ_NUMBER_FIELD)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(ACK_NUMBER_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(SYN_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(ACK_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(PSH_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(FIN_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(RST_FIELD)),
                        bodyRequired ? cursor.getBlob(cursor.getColumnIndexOrThrow(RAW_BODY_FIELD)) : null
                ));
            } while (cursor.moveToNext());
        }
        //logger.trace("cursor.getCount=" + cursor.getCount());
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return packets;
    }

    /**
     * Downloads packets from db in few steps
     *
     * @param startTime    first timestamp
     * @param endTime      last timestamp
     * @param bodyRequired download packets with body
     * @param partly       downloads packets in few steps
     * @return All tcp packets from db which have timestamp between startTime and endTime
     */
    public List<TcpPacket> getTcpPackets(long startTime, long endTime, boolean bodyRequired, boolean partly) {
        if (!partly) return getTcpPackets(startTime, endTime, bodyRequired);
        List<TcpPacket> packets = new ArrayList<TcpPacket>();
        long delta = (endTime - startTime) / 10;
        long currentEndTime;

        while (startTime < endTime) {
            currentEndTime = startTime + delta;
            if (currentEndTime > endTime) currentEndTime = endTime;
            packets.addAll(getTcpPackets(startTime, currentEndTime, bodyRequired));
            startTime += delta;
        }
        Set<TcpPacket> packetSet = new HashSet<TcpPacket>(packets);
        packets.clear();
        packets.addAll(packetSet);
        return packets;
    }

    public List<DnsPacket> getDnsPackets(long startTime, long endTime, boolean bodyRequired) {
        List<DnsPacket> packets = new ArrayList<DnsPacket>();
        String[] columnsIncludingBody = new String[]{TIMESTAMP_FIELD, TOTAL_LENGTH_FIELD, PAYLOAD_LENGTH_FIELD,
                INTERFACE_FIELD, DIRECTION_FIELD, SRC_ADDR_FIELD, DEST_ADDR_FIELD, SRC_PORT_FIELD, DEST_PORT_FIELD,
                TRANSACTION_ID_FIELD, HOST_FIELD, RAW_BODY_FIELD};
        String[] columnsExcludingBody = Arrays.copyOfRange(columnsIncludingBody, 0, columnsIncludingBody.length - 1);
        Cursor cursor = dataBase.query(
                DNS_PACKETS_NAME_TABLE,
                bodyRequired ? columnsIncludingBody : columnsExcludingBody,
                TIME_CONDITION,
                new String[]{String.valueOf(startTime), String.valueOf(endTime)},
                null, null, ID_FIELD);
        if (cursor.moveToFirst()) {
            do {
                packets.add(PacketBuilder.buildDnsPacket(
                        cursor.getLong(cursor.getColumnIndexOrThrow(TIMESTAMP_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TOTAL_LENGTH_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(PAYLOAD_LENGTH_FIELD)),
                        Interface.values()[cursor.getInt(cursor.getColumnIndexOrThrow(INTERFACE_FIELD))],
                        Direction.values()[cursor.getInt(cursor.getColumnIndexOrThrow(DIRECTION_FIELD))],
                        cursor.getString(cursor.getColumnIndexOrThrow(SRC_ADDR_FIELD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DEST_ADDR_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(SRC_PORT_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DEST_PORT_FIELD)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TRANSACTION_ID_FIELD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(HOST_FIELD)),
                        bodyRequired ? cursor.getBlob(cursor.getColumnIndexOrThrow(RAW_BODY_FIELD)) : null
                ));
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return packets;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            StringBuilder stringBuilder = new StringBuilder();
            db.execSQL(stringBuilder.append("CREATE TABLE ").append(TCP_PACKETS_NAME_TABLE).append(" (").append(ID_FIELD)
                    .append(" integer primary key autoincrement, ").append(TIMESTAMP_FIELD).append(INTEGER_TYPE)
                    .append(TOTAL_LENGTH_FIELD).append(INTEGER_TYPE).append(PAYLOAD_LENGTH_FIELD).append(INTEGER_TYPE)
                    .append(INTERFACE_FIELD).append(INTEGER_TYPE).append(DIRECTION_FIELD).append(INTEGER_TYPE)
                    .append(SRC_ADDR_FIELD).append(TEXT_TYPE).append(DEST_ADDR_FIELD).append(TEXT_TYPE)
                    .append(SRC_PORT_FIELD).append(INTEGER_TYPE).append(DEST_PORT_FIELD).append(INTEGER_TYPE)
                    .append(SYN_FIELD).append(INTEGER_TYPE).append(ACK_FIELD).append(INTEGER_TYPE).append(PSH_FIELD)
                    .append(INTEGER_TYPE).append(FIN_FIELD).append(INTEGER_TYPE).append(RST_FIELD).append(INTEGER_TYPE)
                    .append(SEQ_NUMBER_FIELD).append(INTEGER_TYPE).append(ACK_NUMBER_FIELD).append(INTEGER_TYPE)
                    .append(RAW_BODY_FIELD).append(" blob);").toString());
            stringBuilder.delete(0, stringBuilder.length());
            db.execSQL(stringBuilder.append("CREATE TABLE ").append(DNS_PACKETS_NAME_TABLE).append(" (").append(ID_FIELD)
                    .append(" integer primary key autoincrement, ").append(TIMESTAMP_FIELD).append(INTEGER_TYPE)
                    .append(TOTAL_LENGTH_FIELD).append(INTEGER_TYPE).append(PAYLOAD_LENGTH_FIELD).append(INTEGER_TYPE)
                    .append(INTERFACE_FIELD).append(INTEGER_TYPE).append(DIRECTION_FIELD).append(INTEGER_TYPE)
                    .append(SRC_ADDR_FIELD).append(TEXT_TYPE).append(DEST_ADDR_FIELD).append(TEXT_TYPE)
                    .append(SRC_PORT_FIELD).append(INTEGER_TYPE).append(DEST_PORT_FIELD).append(INTEGER_TYPE)
                    .append(HOST_FIELD).append(TEXT_TYPE).append(TRANSACTION_ID_FIELD).append(INTEGER_TYPE)
                    .append(RAW_BODY_FIELD).append(" blob);").toString());
        } catch (SQLException e) {
            logger.error(String.format("Failed to deploy %s database. Reason: %s", DATABASE_NAME, e.getMessage()));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        logger.warn(String.format("Upgrading DB from version %d to %d.", oldVersion, newVersion));
        db.execSQL("DROP TABLE IF EXISTS " + TCP_PACKETS_NAME_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DNS_PACKETS_NAME_TABLE);
        onCreate(db);
    }

    private static void insertEntry(String tableName, ContentValues contentValues) {
        dataBase.insert(tableName, null, contentValues);
    }

    public static void closeDataBase() {
        if (dataBase != null) {
            dataBase.close();
            dataBase = null;
        }
    }

    public static void copyDb() {
        String outputFilename = "/data/data/com.seven.asimov.it/databases/reporting" + Integer.toString(++databaseCoefficient) + ".db";
        logger.info("copyDb: outputFilename=" + outputFilename);
        String[] corruptCE = {"su", "-c", "cat " + reportDataBaseFilename + " > " + outputFilename};
        try {
            Runtime.getRuntime().exec(corruptCE).waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void openCopiedDataBase() {
        logger.info(String.format("Current reporting coefficient: %d", databaseCoefficient));
        String databaseFilename = "/data/data/com.seven.asimov.it/databases/reporting" + Integer.toString(databaseCoefficient) + ".db";
        logger.info(String.format("su -c chmod 777 %s", databaseFilename));
        String[] chmod = {"su", "-c", "chmod 777 " + databaseFilename};
        try {
            Runtime.getRuntime().exec(chmod).waitFor();
        } catch (Exception e) {
            logger.debug(ExceptionUtils.getStackTrace(e));
        }
        //Log.v(TAG, String.format(Shell.execSimple(String.format("su -c chmod 777 %s", databaseFilename))));
        dataBase = SQLiteDatabase.openDatabase(databaseFilename, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    public static List<ReportWrapper> getReportEntries() throws Exception {
        List<ReportWrapper> result = new ArrayList<ReportWrapper>();
        String[] columns = new String[]{ID_FIELD, TYPE_FIELD, DDATA_FIELD};
        copyDb();
        openCopiedDataBase();
        Cursor cursor = dataBase.query(REPORT_TABLE_NAME, columns, null, null, null, null, ID_FIELD);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int type = cursor.getInt(1);
                byte[] data = cursor.getBlob(2);
                ReportWrapper wrapper = new ReportWrapper(id, type, data);
                result.add(wrapper);
            } while (cursor.moveToNext());
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        closeDataBase();
        return result;
    }

    private static class ReportWrapper {

        private int id;
        private int type;
        private ByteBuffer data;

        public ReportWrapper(int id, int type, byte[] bytes) {
            this.id = id;
            this.type = type;
            data = ByteBuffer.allocate(bytes.length);
            data.put(bytes);
        }

        public ReportWrapper(int id, int type, ByteBuffer byteBuffer) {
            this.id = id;
            this.type = type;
            this.data = byteBuffer;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public ByteBuffer getData() {
            return data;
        }

        public void setData(ByteBuffer data) {
            this.data = data;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder("Id=");
            result.append(id);
            result.append(" Type=");
            result.append(type);
            result.append(" Data:\r\n");
            int count = 0;
            for (byte value : data.array()) {
                int intValue = value;
                if (intValue < 0)
                    intValue += 256;
                result.append(Integer.toHexString(intValue));
                result.append(" ");
                if (++count >= 16) {
                    count = 0;
                    result.append("\r\n");
                }
            }
            return result.toString();
        }
    }
}
