package com.foloke.cascade.utils;

import com.lumaserv.netflow.flowset.FlowField;
import com.lumaserv.netflow.flowset.FlowValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class SimpleFlow {
    public int sourceIP;
    public boolean expired = false;
    public int address;
    public int port;
    public Map<FlowField, FlowValue> data;
    public long timestamp;
    public int timeout;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public SimpleFlow(int sourceIP, Map<FlowField, FlowValue> data) {
        this.sourceIP = sourceIP;
        this.timestamp = System.currentTimeMillis();
        this.data = data;
        this.address = data.get(FlowField.IPV4_DST_ADDR).asInt();
        this.port = data.get(FlowField.L4_DST_PORT).asUShort();
//        System.out.println(sdf.format(Calendar.getInstance().getTime())
//                + " " + data.get(FlowField.L4_DST_PORT).asUShort()
//                + " " + ipToString(data.get(FlowField.IPV4_DST_ADDR).asBytes())
//                + " " + ipToString(data.get(FlowField.IPV4_SRC_ADDR).asBytes())
//                + " " + data.get(FlowField.L4_SRC_PORT).asUShort()
//                + " timeout: " + timeout);

        if(data.get(FlowField.PROTOCOL).asByte() == 6) {
            byte flags = data.get(FlowField.TCP_FLAGS).asByte();
            if ((flags & 1) == 1 || (data.get(FlowField.LAST_SWITCHED).asInt()
                    - data.get(FlowField.FIRST_SWITCHED).asInt()) > 0) {
                timeout = 0;
            } else {
                timeout = 1000 * 16; //MicroTick router has default 15s timeout for inactive flows
            }
        }
    }

    public void tick() {
        if (System.currentTimeMillis() - timestamp >= timeout)
            expired = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleFlow that = (SimpleFlow) o;

        if (address != that.address) return false;
        return port == that.port;
    }

    @Override
    public int hashCode() {
        int result = address;
        result = 31 * result + port;
        return result;
    }

    public static String ipToString(byte[] bytes) {
        return "" + (bytes[0] > 0 ? bytes[0] : 256 + bytes[0]) + '.' +
                (bytes[1] > 0 ? bytes[1] : 256 + bytes[1]) + '.' +
                (bytes[2] > 0 ? bytes[2] : 256 + bytes[2]) + '.' +
                (bytes[3] > 0 ? bytes[3] : 256 + bytes[3]);
    }

    private int ipToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                ((bytes[3] & 0xFF));
    }
}
