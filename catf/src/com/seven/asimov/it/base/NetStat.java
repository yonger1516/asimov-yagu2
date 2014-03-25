package com.seven.asimov.it.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class NetStat {

    public static final String DESTINATION_IP = "213.180.28.143";
    public static int HTTP_PORT = 80;
    private BufferedReader reader;
    private Process process;
    private List<SocketInfo> sockets = new ArrayList<SocketInfo>();

    public NetStat() {
        this(true);
    }

    public NetStat(boolean delayed) {
        if (delayed) {
            parse();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    parse();
                }
            }).start();
        }
    }

    private void parse() {
        try {
            String[] a = { "su", "-c", "netstat" };
            Process process = Runtime.getRuntime().exec(a);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String record = reader.readLine();
            record = reader.readLine();
            while (record != null) {
                System.out.println(record);
                parseRecord(record);
                record = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseRecord(String record) {
        String[] recItems = record.trim().split("\\s+");
        SocketInfo si = new SocketInfo();
        si.protocol = recItems[0];
        si.recieved = Integer.valueOf(recItems[1]);
        si.send = Integer.valueOf(recItems[2]);
        si.localAdress = parseIp(recItems[3]);
        si.localPort = parsePort(recItems[3]);
        si.foreignAdress = parseIp(recItems[4]);
        si.foreignPort = parsePort(recItems[4]);
        si.state = recItems[5];
        sockets.add(si);
    }

    private int parsePort(String address) {
        String[] items = address.split(":");
        int result = items[Array.getLength(items) - 1].equalsIgnoreCase("*") ? 0 : Integer.valueOf(items[Array
                .getLength(items) - 1]);
        return result;
    }

    private String parseIp(String address) {
        String[] items = address.split(":");
        return items[Array.getLength(items) - 2];
    }

    public List<SocketInfo> getSockets() {
        return sockets;
    }

    public List<SocketInfo> getSockets(int port) {
        List<SocketInfo> result = new ArrayList<NetStat.SocketInfo>();
        for (NetStat.SocketInfo s : sockets) {
            if (s.getForeignPort() == port) {
                result.add(s);
            }
        }
        return result;
    }

    public boolean socketWithDistanationExists(String foreignAdress, int foreignPort) {
        boolean result = false;
        for (SocketInfo soc : getSockets()) {
            if (soc.foreignAdress.equalsIgnoreCase(foreignAdress) && soc.foreignPort == foreignPort) {
                result = true;
            }
        }
        return result;
    }

    public static class SocketInfo implements Parcelable {
        private String protocol;
        private int recieved;
        private int send;
        private String localAdress;
        private int localPort;
        private String foreignAdress;
        private int foreignPort;
        private String state;

        public static final Parcelable.Creator<SocketInfo> CREATOR = new Parcelable.Creator<SocketInfo>() {
            public SocketInfo createFromParcel(Parcel in) {
                return new SocketInfo(in);
            }

            public SocketInfo[] newArray(int size) {
                return new SocketInfo[size];
            }
        };

        public SocketInfo(Parcel in) {
            readFromParcel(in);
        }

        public int describeContents() {
            return 0;
        }

        public void readFromParcel(Parcel in) {
            protocol = in.readString();
            recieved = in.readInt();
            send = in.readInt();
            localAdress = in.readString();
            localPort = in.readInt();
            foreignAdress = in.readString();
            foreignPort = in.readInt();
            state = in.readString();
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(protocol);
            out.writeInt(recieved);
            out.writeInt(send);
            out.writeString(localAdress);
            out.writeInt(localPort);
            out.writeString(foreignAdress);
            out.writeInt(foreignPort);
            out.writeString(state);
        }

        public String getLocalAdress() {
            return localAdress;
        }

        public void setLocalAdress(String localAdress) {
            this.localAdress = localAdress;
        }

        public int getLocalPort() {
            return localPort;
        }

        public void setLocalPort(int localPort) {
            this.localPort = localPort;
        }

        public String getForeignAdress() {
            return foreignAdress;
        }

        public void setForeignAdress(String foreignAdress) {
            this.foreignAdress = foreignAdress;
        }

        public int getForeignPort() {
            return foreignPort;
        }

        public void setForeignPort(int foreignPort) {
            this.foreignPort = foreignPort;
        }

        public SocketInfo() {
        }

        public SocketInfo(String localAdress, int localPort, String foreignAdress, int foreignPort) {
            this.localAdress = localAdress;
            this.foreignPort = foreignPort;
            this.foreignAdress = foreignAdress;
            this.localPort = localPort;
        }

        @Override
        public String toString() {
            String result = localAdress + " " + localPort + " " + foreignAdress + " " + foreignPort;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            boolean result = false;

            if (o instanceof NetStat.SocketInfo
                    && this.localAdress.equalsIgnoreCase(((NetStat.SocketInfo) o).localAdress)
                    && this.localPort == ((NetStat.SocketInfo) o).localPort
                    && this.foreignAdress.equalsIgnoreCase(((NetStat.SocketInfo) o).foreignAdress)
                    && this.foreignPort == ((NetStat.SocketInfo) o).foreignPort) {
                result = true;
            }
            return result;
        }

        @Override
        public int hashCode() {
            String[] locadd = localAdress.trim().split("\\.");
            String[] foradd = foreignAdress.trim().split("\\.");
            return foreignPort + localPort + Integer.valueOf(locadd[0]) + Integer.valueOf(locadd[1])
                    + Integer.valueOf(locadd[2]) + Integer.valueOf(locadd[3]) + Integer.valueOf(foradd[0])
                    + Integer.valueOf(foradd[1]) + Integer.valueOf(foradd[2]) + Integer.valueOf(foradd[3]);
        }
    }

}