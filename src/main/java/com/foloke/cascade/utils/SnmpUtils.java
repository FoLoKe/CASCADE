package com.foloke.cascade.utils;

import com.foloke.cascade.Application;
import com.foloke.cascade.Controllers.UIController;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Port;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.TSM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SnmpUtils {

    public static OID interfacesIDS = new OID(".1.3.6.1.2.1.2.2.1.1");
    public static OID interfacesMAC = new OID(".1.3.6.1.2.1.2.2.1.6");
    public static OID interfacesState = new OID(".1.3.6.1.2.1.2.2.1.8");
    public static OID interfacesDescription = new OID(".1.3.6.1.2.1.2.2.1.2");
    public static OID addressesAddress = new OID(".1.3.6.1.2.1.4.20.1.1");
    public static OID addressesIDS = new OID(".1.3.6.1.2.1.4.20.1.2");
    public static OID routingIDS = new OID(".1.3.6.1.2.1.4.22.1.1");
    public static OID routingMAC = new OID(".1.3.6.1.2.1.4.22.1.2");

    public static void getRequest(Target<UdpAddress> communityTarget, UsmUser user, OID oid) {
        SnmpGet snmpGet = new SnmpGet(communityTarget, user, oid);
        Thread thread = new Thread(snmpGet);
        thread.start();
    }

    public static void walkRequest(Target<UdpAddress> target, UsmUser user, OID tableOid, List<UIController.Property> props) {
        SnmpWalk snmpWalk = new SnmpWalk(target, user, tableOid, props);
        Thread thread = new Thread(snmpWalk);
        thread.start();
    }

    public static void initDevice(Device device) {
        DeviceInitiator deviceInitiator = new DeviceInitiator(device);
        Thread thread = new Thread(deviceInitiator);
        thread.start();
    }

    private static class DeviceInitiator implements Runnable {
        Device device;

        public DeviceInitiator(Device device) {
            this.device = device;
        }

        @Override
        public void run() {
            TreeMap<OID, String> interfacesInfo = walkToOIDStringMap(interfacesIDS, device);
            TreeMap<OID, String> addressesInfo = walkToOIDStringMap(addressesAddress, device);

            Map<String, String> addressesMap = new TreeMap<>();
            for (Map.Entry<OID, String> entry : addressesInfo.entrySet()) {
                VariableBinding variableBinding = getFirst(new OID(addressesIDS + "." + entry.getValue() + ".0"), device);

                if (variableBinding != null) {
                    addressesMap.put(variableBinding.getVariable().toString(), entry.getValue());
                }
            }

            for (Map.Entry<OID, String> entry : interfacesInfo.entrySet()) {
                Port port = new Port(device, entry.getValue(), 0);
                port.index = Integer.parseInt(entry.getValue());
                port.addType = Port.AddType.SNMP;

                VariableBinding macBinding = getFirst(new OID(interfacesMAC + "." + entry.getValue() + ".0"), device);
                if (macBinding != null) {
                    port.mac = macBinding.getVariable().toString();
                }

                VariableBinding nameBinding = getFirst(new OID(interfacesDescription + "." + entry.getValue() + ".0"), device);
                if (nameBinding != null) {
                    port.setName(nameBinding.getVariable().toString());
                }

                VariableBinding stateBinding = getFirst(new OID(interfacesState + "." + entry.getValue() + ".0"), device);
                if (stateBinding != null) {
                    port.setState(stateBinding.getVariable().toInt());
                }

                port.primaryAddress = addressesMap.get(Integer.toString(port.index));
                if (port.primaryAddress == null) {
                    port.primaryAddress = "";
                }

                port = device.addOrUpdatePort(port);

                if (port.primaryAddress.length() > 0) {
                    List<OID> routingInfo = walkToOIDList(new OID(routingIDS + "." + entry.getValue()), device);

                    for (OID routeOID : routingInfo) {
                        String address = routeOID.getSuffix(new OID(routingIDS + "." + entry.getValue())).toString();
                        Port leadingPort = device.mapController.findPort(address);

                        if (leadingPort == null) {
                            String mac = null;

                            VariableBinding rotingBinding = getFirst(new OID(routingMAC + "." + entry.getValue() + "." + address + ".0"), device);
                            if (rotingBinding != null) {
                                mac = rotingBinding.getVariable().toString();
                            }

                            if (mac != null) {
                                Device leadingDevice = new Device(device.mapController, "127.0.0.1");
                                leadingPort = new Port(leadingDevice, address, 0);
                                leadingPort = leadingDevice.addOrUpdatePort(leadingPort);
                                leadingPort.mac = mac;
                                device.mapController.addEntity(leadingDevice);
                            }
                        }
                        device.mapController.establishConnection(port, leadingPort);
                    }

                }
            }
        }
    }

    private static VariableBinding getFirst(OID oid, Device device) {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);

        List<? extends VariableBinding> info = get(pdu, device.target, device.user);
        if (info != null) {
            for (VariableBinding varBinding : info) {
                if (varBinding == null) {
                    continue;
                }
                return varBinding;
            }
        }

        return null;
    }

    private static List<OID> walkToOIDList(OID oid, Device device) {
        List<TreeEvent> events = walk(oid, device.target, device.user);
        List<OID> info = new ArrayList<>();

        if (events != null) {
            for (TreeEvent event : events) {
                if (valid(event)) {
                    VariableBinding[] varBindings = event.getVariableBindings();
                    for (VariableBinding varBinding : varBindings) {
                        if (varBinding == null) {
                            continue;
                        }
                        info.add(varBinding.getOid());
                    }
                }
            }
        }

        return info;
    }

    private static TreeMap<OID, String> walkToOIDStringMap(OID oid, Device device) {
        TreeMap<OID, String> treeMap = new TreeMap<>();

        List<TreeEvent> events = walk(oid, device.target, device.user);

        if (events != null) {
            for (TreeEvent event : events) {
                if (valid(event)) {
                    VariableBinding[] varBindings = event.getVariableBindings();
                    for (VariableBinding varBinding : varBindings) {
                        if (varBinding == null) {
                            continue;
                        }
                        treeMap.put(varBinding.getOid(), varBinding.getVariable().toString());
                    }
                }
            }

        }

        return treeMap;
    }

    private static class SnmpWalk implements Runnable {
        OID oid;
        Target<UdpAddress> target;
        List<UIController.Property> propsList;
        UsmUser user;

        public SnmpWalk(Target<UdpAddress> communityTarget, UsmUser user, OID oid, List<UIController.Property> propsList) {
            this.user = user;
            this.target = communityTarget;
            this.oid = oid;
            this.propsList = propsList;
        }

        @Override
        public void run() {
            try {
                List<TreeEvent> events = walk(oid, target, user);

                if(events != null) {
                    for (TreeEvent event : events) {
                        if (event == null) {
                            continue;
                        }
                        if (event.isError()) {
                            LogUtils.log(" Error: table OID [" + oid + "] " + event.getErrorMessage());
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
                            propsList.add(new UIController.Property(varBinding.getOid().toString(), varBinding.getVariable().toString()));
                        }

                    }
                }

            } catch (Exception e) {
                LogUtils.log(e.toString());
            }

        }
    }

    private static class SnmpGet implements Runnable {
        PDU pdu;
        Target<UdpAddress> target;
        UsmUser usmUser;
        public SnmpGet(Target<UdpAddress> communityTarget, UsmUser user, OID oid) {
            this.usmUser = user;
            this.target = communityTarget;
            pdu = new PDU();
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);
        }

        @Override
        public void run() {
            get(pdu, target, usmUser);
        }
    }

    private static List<TreeEvent> walk(OID oid, Target<UdpAddress> target, UsmUser user) {
        try {
            TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);

            if(target.getVersion() == SnmpConstants.version3) {
                snmp.getUSM().addUser(user.getSecurityName(), user);
                SecurityModels.getInstance().addSecurityModel(new TSM(Application.localEngineId, false));
            }

            transport.listen();

            TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
            List<TreeEvent> events = treeUtils.getSubtree(target, oid);
            if (events == null || events.size() == 0) {
                LogUtils.log("Error: Unable to read table...");
                snmp.close();
                transport.close();
                return null;
            }
            snmp.close();
            transport.close();
            return events;

        } catch (Exception e) {
            LogUtils.log(e.toString());
        }
        return null;
    }

    private static List<? extends VariableBinding> get(PDU pdu, Target<UdpAddress> target, UsmUser user) {
        List<? extends VariableBinding> vbs = null;
        try {
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);

            if(target.getVersion() == SnmpConstants.version3) {
                snmp.getUSM().addUser(user.getSecurityName(), user);
                SecurityModels.getInstance().addSecurityModel(new TSM(Application.localEngineId, false));
                ScopedPDU scopedPDU = new ScopedPDU();
                scopedPDU.addAll(pdu.getVariableBindings());
                scopedPDU.setType(PDU.GET);
                pdu = scopedPDU;
            }

            transport.listen();
            ResponseEvent<UdpAddress> responseEvent = snmp.get(pdu, target);
            PDU response = responseEvent.getResponse();
            if(response == null) {
                LogUtils.log("Time out: " + responseEvent.getError());
            }else if(response.getErrorStatus() == PDU.noError) {
                vbs = response.getVariableBindings();
                for (VariableBinding vb : vbs) {
                    LogUtils.log(target.getAddress() + " SNMP response for OID: " + vb.getOid().toString()  + " is: "
                    + vb.getVariable().toString());
                }

            } else {
                LogUtils.log(response.getErrorStatusText());
            }
            snmp.close();
            transport.close();
        } catch (Exception e) {
            LogUtils.log(e.toString());
        }
        return vbs;
    }

    public static boolean valid(TreeEvent event) {
        if (event == null) {
            return false;
        }
        if (event.isError()) {
            LogUtils.log(" Error: table OID [" + interfacesIDS + "] " + event.getErrorMessage());
            return false;
        }

        VariableBinding[] varBindings = event.getVariableBindings();
        return varBindings != null && varBindings.length != 0;
    }
}
