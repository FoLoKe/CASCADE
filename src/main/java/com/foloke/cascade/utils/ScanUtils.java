package com.foloke.cascade.utils;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Port;
import org.apache.commons.net.util.SubnetUtils;
import org.pcap4j.core.*;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.ByteArrays;
import org.pcap4j.util.MacAddress;
import org.pcap4j.util.NifSelector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.regex.Pattern;

public class ScanUtils {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String regexIPv4 = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
    private static final Pattern pattern = Pattern.compile(regexIPv4);

    public static void scanByPing(MapController mapController, String network, String mask) {
        try {
            SubnetUtils utils = new SubnetUtils( network + "/" + mask);

            for(String address : utils.getInfo().getAllAddresses()) {

                Ping ping = new Ping(mapController, address);
                Thread thread = new Thread(ping);
                thread.start();
            }

        } catch (Exception e) {
            LogUtils.log(e.toString());
        }
    }

    public static void ping(Port port) {
        if(port.primaryAddress.length() > 0) {
            Ping ping = new Ping(port);
            Thread thread = new Thread(ping);
            thread.start();
        }
    }

    public static void traceRoute(MapController mapController, String destination, int timeout, int maxHops) {
        Trace trace = new Trace(mapController, destination, timeout, maxHops);
        Thread thread = new Thread(trace);
        thread.start();
    }

    public static void requestMac(Port port) {
        PcapNetworkInterface nif;
        InetAddress senderIp;
        MacAddress senderMac;
        try {
            nif = new NifSelector().selectNetworkInterface();
            if (nif == null)
                return;
            senderIp = nif.getAddresses().get(0).getAddress();
            senderMac = MacAddress.getByAddress(nif.getLinkLayerAddresses().get(0).getAddress());
            System.out.println(senderIp);
            System.out.println(senderMac);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        PcapHandle handle;
        PcapHandle sendHandle;

        try {
            handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
            sendHandle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            handle.setFilter(
                    "arp and src host "
                            + port.primaryAddress
                            + " and dst host "
                            + senderIp.getHostAddress()
                            + " and ether dst "
                            + Pcaps.toBpfString(senderMac),
                    BpfProgram.BpfCompileMode.OPTIMIZE);

            PacketListener listener =
                    new PacketListener() {
                        @Override
                        public void gotPacket(Packet packet) {
                            if (packet.contains(ArpPacket.class)) {
                                ArpPacket arp = packet.get(ArpPacket.class);
                                if (arp.getHeader().getOperation().equals(ArpOperation.REPLY)) {
                                    System.out.println(arp.getHeader().getSrcHardwareAddr());
                                }
                            }
                            System.out.println(packet);
                        }
                    };

            ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
            arpBuilder
                    .hardwareType(ArpHardwareType.ETHERNET)
                    .protocolType(EtherType.IPV4)
                    .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                    .protocolAddrLength((byte) ByteArrays.INET4_ADDRESS_SIZE_IN_BYTES)
                    .operation(ArpOperation.REQUEST)
                    .srcHardwareAddr(senderMac)
                    .srcProtocolAddr(senderIp)
                    .dstHardwareAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                    .dstProtocolAddr(InetAddress.getByName(port.primaryAddress));

            EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
            etherBuilder
                    .dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                    .srcAddr(senderMac)
                    .type(EtherType.ARP)
                    .payloadBuilder(arpBuilder)
                    .paddingAtBuild(true);
            Packet p = etherBuilder.build();

            PCapTask t = new PCapTask(handle, listener);
            Thread pcapThread = new Thread(t);
            pcapThread.setDaemon(true);
            pcapThread.start();
            sendHandle.sendPacket(p);
            sendHandle.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Trace implements Runnable {
        MapController mapController;
        String address;
        int timeout;
        int maxHops;

        public Trace(MapController mapController, String address, int timeout, int maxHops) {
            this.mapController = mapController;
            this.address = address;
            this.timeout = timeout;
            this.maxHops = maxHops;
        }

        @Override
        public void run() {
//            Device local = initLocal(mapController);
//            Device previousHop;
//            if(local.getPorts().size() > 0) {
//                Port port = mapController.findPort(local.getPorts().get(0).primaryAddress);
//                if(port != null) {
//                    previousHop = port.parent;
//                } else {
//                    mapController.addEntity(local);
//                    previousHop = local;
//                }
//
//                try {
//                    List<String> hops = trace(address, timeout, maxHops);
//                    if(hops.get(hops.size() - 1).equals(address)) {
//                        LogUtils.log("tracing succeed");
//                        for (String hop : hops) {
//                            SubnetUtils subnetUtils = new SubnetUtils(hop + "/" + 24);
//                            Port portA = null;
//                            Port portB = null;
//                            //Device device = mapController.addOrUpdate(hop);
//
//                            for (Port previousHopPort : previousHop.getPorts()) {
//                                if (previousHopPort.isInRange(hop)) {
//                                    portB = previousHopPort;
//                                }
//                            }
//                            if (portB == null) {
//                                portB = previousHop.addPort("");
//                                portB.setName("unknown");
//                                portB.primaryAddress = subnetUtils.getInfo().getNetworkAddress();
//                            }
//
//                            portB.setState(Port.State.UP);
//
//                            for (Port nextHopPort : device.getPorts()) {
//                                if (nextHopPort.isInRange(hop)) {
//                                    portA = nextHopPort;
//                                }
//                            }
//                            if (portA == null) {
//                                portA = device.addPort("");
//                                portA.setName("unknown");
//                                portA.primaryAddress = hop;
//                            }
//                            portA.setState(Port.State.UP);
//
//                            //mapController.establishConnection(portA, portB);
//
//                            previousHop = device;
//                        }
//                    } else {
//                        LogUtils.log("tracing fail");
//                    }
//
//
//                } catch (Exception e) {
//                    LogUtils.log(e.toString());
//                }
//            }
        }

        private static List<String> trace(String address, int timeout, int maxHops) throws IOException {

            if (OS.contains("win")) {
                return cmdWindowsTraceroute(address, timeout, maxHops);
            } else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
                return  cmdLinuxTraceroute(address, timeout, maxHops);
            }

            return new ArrayList<>();
        }

        private static List<String> cmdWindowsTraceroute(String ipAddress, int timeout, int maxHops) throws IOException {
            Runtime runTime = Runtime.getRuntime();
            Process process;
            List<String> cmdResult = new ArrayList<>();
            String line;
            String param = "tracert -d -h " + maxHops + " -w " + timeout + " " + ipAddress;
            process = runTime.exec("cmd /c chcp 437 & " + param);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                while ((line = bufferedReader.readLine()) != null) {

                    String[] splited = line.split(" ");
                    String address = splited[splited.length - 1];
                    if(pattern.matcher(address).matches()) {
                        cmdResult.add(address);
                    }

                    LogUtils.log(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return cmdResult;
            }

            return cmdResult;
        }

        private static List<String> cmdLinuxTraceroute(String ipAddress, int timeout, int maxHops) throws IOException {
            List<String> commands = new ArrayList<>();

            commands.add("traceroute");
            commands.add("-n");
            commands.add("-w " + timeout / 1000);
            commands.add("-m " + maxHops);    //hop limit
            commands.add(ipAddress);

            ProcessBuilder pb = new ProcessBuilder(commands);
            Process process = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String str;
            List<String> cmdResult = new ArrayList<>();

            try {
                while ((str = stdInput.readLine()) != null) {
                    String[] splited = str.split(" ");
                    cmdResult.add(splited[0]);
                    LogUtils.log(splited[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return cmdResult;
            }

            return cmdResult;
        }
    }

    private static class Ping implements Runnable {
        MapController mapController;
        InetAddress inetAddress;
        Port port;

        public Ping(MapController mapController, String address) {
            this.mapController = mapController;
            try {
                this.inetAddress = InetAddress.getByName(address);
            } catch (Exception e) {
                LogUtils.log(e.toString());
            }
        }

        public Ping(Port port) {
            this.port = port;
            try {
                this.inetAddress = InetAddress.getByName(port.primaryAddress);
            } catch (Exception e) {
                LogUtils.log(e.toString());
            }
        }

        @Override
        public void run() {
            try {
                boolean reachable = inetAddress.isReachable(1000);

                if(reachable) {
                    if(port == null) {
                        //Device device = mapController.addOrUpdate(inetAddress.getHostAddress());
                        //port = device.findPort(inetAddress.getHostAddress());
                    }
                }

                LogUtils.log(inetAddress.getHostAddress() + " reachable: " + reachable);
                if(port != null) {
                    if(reachable) {
                        port.setState(Port.State.UP);
                    } else {
                        port.setState(Port.State.DOWN);
                    }
                    LogUtils.logToFile(port.parent.getName(), "port with address: " + port.primaryAddress + "is reachable ?: " + reachable);
                }
            } catch (Exception e) {
                LogUtils.log(e.toString());
            }
        }
    }

    public static class PCapTask implements Runnable {

        private final PcapHandle handle;
        private final PacketListener listener;

        public PCapTask(PcapHandle handle, PacketListener listener) {
            this.handle = handle;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                handle.loop(1, listener);
                handle.close();
            } catch (PcapNativeException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }
    }

    public static Device initLocal(MapController mapController) {
        List<NetworkInterface> realInterfaces = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            Iterator<NetworkInterface> iterator = interfaces.asIterator();
            while (iterator.hasNext()) {
                NetworkInterface networkInterface = iterator.next();
                if (!networkInterface.isLoopback()
                        && !networkInterface.isVirtual()
                        && networkInterface.getHardwareAddress() != null) {
                    realInterfaces.add(networkInterface);
                }
            }

            Device entity;
            if (realInterfaces.size() > 0) {
                String address = realInterfaces.get(0).getInterfaceAddresses().get(0).getAddress().getHostAddress();
                entity = new Device(mapController, address);
                for (int i = 1; i < realInterfaces.size(); i++) {
                    entity.addPort(new Port(entity, realInterfaces.get(i)));
                }
            } else {
                entity = new Device(mapController, "127.0.0.1");
            }

            return entity;

        } catch (SocketException e) {
            LogUtils.log(e.toString());
            e.printStackTrace();
        }

        return null;
    }
}
