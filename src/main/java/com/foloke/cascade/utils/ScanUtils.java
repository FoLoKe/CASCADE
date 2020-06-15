package com.foloke.cascade.utils;

import com.foloke.cascade.Application;
import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.Entities.Device;
import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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

    public static void ping(Device.Port port) {
        if(port.address.length() > 0) {
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
            Device local = initLocal(mapController);
            Device previousHop;
            if(local.getPorts().size() > 0) {
                Device.Port port = mapController.findPort(local.getPorts().get(0).address);
                if(port != null) {
                    previousHop = port.parent;
                } else {
                    mapController.addEntity(local);
                    previousHop = local;
                }

                try {
                    List<String> hops = trace(address, timeout, maxHops);
                    if(hops.get(hops.size() - 1).equals(address)) {
                        LogUtils.log("tracing succeed");
                        for (String hop : hops) {
                            SubnetUtils subnetUtils = new SubnetUtils(hop + "/" + 24);
                            Device.Port portA = null;
                            Device.Port portB = null;
                            Device device = mapController.addOrUpdate(hop);

                            for (Device.Port previousHopPort : previousHop.getPorts()) {
                                if (previousHopPort.isInRange(hop)) {
                                    portB = previousHopPort;
                                }
                            }
                            if (portB == null) {
                                portB = previousHop.addPort("");
                                portB.setName("unknown");
                                portB.address = subnetUtils.getInfo().getNetworkAddress();
                            }

                            portB.setState(Device.Port.State.UP);

                            for (Device.Port nextHopPort : device.getPorts()) {
                                if (nextHopPort.isInRange(hop)) {
                                    portA = nextHopPort;
                                }
                            }
                            if (portA == null) {
                                portA = device.addPort("");
                                portA.setName("unknown");
                                portA.address = hop;
                            }
                            portA.setState(Device.Port.State.UP);

                            mapController.establishConnection(portA, portB);

                            previousHop = device;
                        }
                    } else {
                        LogUtils.log("tracing fail");
                    }


                } catch (Exception e) {
                    LogUtils.log(e.toString());
                }
            }
        }
    }

    private static class Ping implements Runnable {
        MapController mapController;
        InetAddress inetAddress;
        Device.Port port;

        public Ping(MapController mapController, String address) {
            this.mapController = mapController;
            try {
                this.inetAddress = InetAddress.getByName(address);
            } catch (Exception e) {
                LogUtils.log(e.toString());
            }
        }

        public Ping(Device.Port port) {
            this.port = port;
            try {
                this.inetAddress = InetAddress.getByName(port.address);
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
                        Device device = mapController.addOrUpdate(inetAddress.getHostAddress());
                        port = device.findPort(inetAddress.getHostAddress());
                    }
                }

                LogUtils.log(inetAddress.getHostAddress() + " reachable: " + reachable);
                if(port != null) {
                    if(reachable) {
                        port.setState(Device.Port.State.UP);
                    } else {
                        port.setState(Device.Port.State.DOWN);
                    }
                    LogUtils.logToFile(port.parent.getName(), "port with address: " + port.address + "is reachable ?: " + reachable);
                }
            } catch (Exception e) {
                LogUtils.log(e.toString());
            }
        }
    }

    private static List<String> trace(String address, int timeout, int maxHops) throws IOException {

        if (OS.contains("win")) {
            return cmdWindowsTraceroute(address, timeout, maxHops);
        } else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
            return  cmdLinuxTraceroute(address, timeout, maxHops);
        }

        return null;
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
            return null;
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
            return null;
        }

        return cmdResult;
    }

    public static Device initLocal(MapController mapController) {
        Device entity = new Device(Application.image, mapController);
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                if (!networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                    LogUtils.log(networkInterface.toString());
                    Device.Port port = entity.addPort(networkInterface);
                } else {
                    LogUtils.log(networkInterface + " is loopback or virtual");
                }
            }
        } catch (SocketException e) {
            LogUtils.log(e.toString());
        }

        return entity;
    }
}
