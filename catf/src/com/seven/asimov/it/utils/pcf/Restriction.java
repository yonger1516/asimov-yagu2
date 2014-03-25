package com.seven.asimov.it.utils.pcf;

import android.util.Log;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;


import static com.seven.asimov.it.utils.pcf.PcfHelper.InterfaceType;

public class Restriction {
    private static final String TAG = Restriction.class.getSimpleName();
    private String protocol;
    private InterfaceType interfaceType;
    private String sPort;
    private String packageName;
    private String ip;
    private String icmpType;

    public static Restriction toRestrictionFromJson(String json) {
        try {
            JSONObject restriction = new JSONObject(json);

            String protocol = restriction.getString("protocol");
            String interfaceType = restriction.getString("networkInterface");
            String sPort = restriction.getString("port");
            String packageName = restriction.getString("packageName");
            String ip = restriction.getString("ip");
            String icmpType="";
            try{
                JSONObject extraIcmp=restriction.getJSONObject("extraProps");
                icmpType=extraIcmp.getString("icmpType");
            }catch(JSONException e){}

            return new Restriction(protocol, InterfaceType.getEnum(interfaceType), sPort, packageName, ip,icmpType);
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        Log.e(TAG, "Unable to create create Restriction from json");
        //TODO should be custom exception
        return null;
    }

    public Restriction(String protocol, InterfaceType interfaceType, String sPort, String packageName, String ip,String icmpCode) {
        this.protocol = protocol;
        this.interfaceType = interfaceType;
        this.sPort = sPort;
        this.packageName = packageName;
        this.ip = ip;
        this.icmpType =icmpCode;
    }

    public JSONObject toJson() {
        JSONObject resultJson = new JSONObject();

        try {
            resultJson.put("protocol", protocol);
            resultJson.put("networkInterface", interfaceType);
            resultJson.put("port", sPort);
            resultJson.put("packageName", packageName);
            resultJson.put("ip", ip);
            if(icmpType !=null&&!icmpType.equals("")){
                JSONObject icmpExtra=new JSONObject();
                icmpExtra.put("icmpType", icmpType);
                resultJson.put("extraProps",icmpExtra);
            }
        } catch (JSONException e) {
            Log.e(TAG, ExceptionUtils.getStackTrace(e));
        }
        return resultJson;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (getClass() != obj.getClass()) return false;

        Restriction other = (Restriction) obj;

        return new EqualsBuilder()
                .append(protocol, other.getProtocol())
                .append(interfaceType, other.getInterfaceType())
                .append(sPort, other.getsPort())
                .append(packageName, other.getPackageName())
                .append(ip, other.getIp())
                .append(icmpType, other.icmpType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(protocol)
                .append(interfaceType)
                .append(sPort)
                .append(packageName)
                .append(ip)
                .append(icmpType).toHashCode();
    }

    public String getProtocol() {
        return protocol;
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public String getsPort() {
        return sPort;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getIp() {
        return ip;
    }

    public String getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(String icmpType) {
        this.icmpType = icmpType;
    }
}
