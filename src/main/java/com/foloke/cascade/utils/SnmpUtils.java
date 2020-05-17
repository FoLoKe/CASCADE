package com.foloke.cascade.utils;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SnmpUtils {

    public static void getRequest(CommunityTarget<UdpAddress> communityTarget, OID oid) {
        SnmpGet snmpGet = new SnmpGet(communityTarget, oid);
        Thread thread = new Thread(snmpGet);
        thread.start();
    }

    public static void walkRequest(CommunityTarget<UdpAddress> target, OID tableOid) {
        SnmpWalk snmpWalk = new SnmpWalk(target, tableOid);
        Thread thread = new Thread(snmpWalk);
        thread.start();
    }

    private static class SnmpWalk implements Runnable {
        OID oid;
        CommunityTarget<UdpAddress> target;

        public SnmpWalk(CommunityTarget<UdpAddress> communityTarget, OID oid) {
            this.target = communityTarget;
            this.oid = oid;
        }

        @Override
        public void run() {
            try {
                Map<String, String> result = new TreeMap<>();
                TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
                Snmp snmp = new Snmp(transport);
                transport.listen();

                TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
                List<TreeEvent> events = treeUtils.getSubtree(target, oid);
                if (events == null || events.size() == 0) {
                    LogUtils.log("Error: Unable to read table...");
                    return;
                }

                for (TreeEvent event : events) {
                    if (event == null) {
                        continue;
                    }
                    if (event.isError()) {
                        LogUtils.log("Error: table OID [" + oid + "] " + event.getErrorMessage());
                        continue;
                    }

                    VariableBinding[] varBindings = event.getVariableBindings();
                    if (varBindings == null || varBindings.length == 0) {
                        continue;
                    }
                    for (VariableBinding varBinding : varBindings) {
                        if (varBinding == null) {
                            continue;
                        }

                        LogUtils.log("." + varBinding.getOid().toString() + " = " + varBinding.getVariable().toString());
                        result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
                    }

                }
                snmp.close();
            } catch (Exception e) {
                LogUtils.log(e.toString());
            }

        }
    }

    private static class SnmpGet implements Runnable {
        PDU pdu;
        CommunityTarget<UdpAddress> target;
        public SnmpGet(CommunityTarget<UdpAddress> communityTarget, OID oid) {
            this.target = communityTarget;
            pdu = new PDU();
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);
        }

        @Override
        public void run() {
            try {
                TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
                transport.listen();
                Snmp snmp = new Snmp(transport);
                ResponseEvent<UdpAddress> responseEvent = snmp.get(pdu, target);
                PDU response = responseEvent.getResponse();
                if(response == null) {
                    LogUtils.log("Time out: " + responseEvent.getError());
                }else if(response.getErrorStatus() == PDU.noError) {
                    List<? extends VariableBinding> vbs = response.getVariableBindings();
                    for (VariableBinding vb : vbs) {
                        LogUtils.log(vb.getVariable().toString());
                    }
                } else {
                    LogUtils.log(response.getErrorStatusText());
                }
                snmp.close();
                transport.close();
            } catch (Exception e) {
                LogUtils.log(e.toString());
            }
        }
    }
}
