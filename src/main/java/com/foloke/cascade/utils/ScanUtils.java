package com.foloke.cascade.utils;

import com.foloke.cascade.Controllers.MapController;
import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
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

        } catch (Exception exception) {
            System.out.println(exception);
        }
    }

    public static void traceRoute(MapController mapController, String destination) {
        Trace trace = new Trace(mapController, destination);
        Thread thread = new Thread(trace);
        thread.start();
    }

    static class Trace implements Runnable {
        MapController mapController;
        String address;

        public Trace(MapController mapController, String address) {
            this.mapController = mapController;
            this.address = address;
        }

        @Override
        public void run() {
            try {
                List<String> hops = trace(address, 1000, 10);
                if(hops.get(hops.size() - 1).equals(address)) {
                    System.out.println("tracing succeed");
                } else {
                    System.out.println("tracing fail");
                }

                for (String hop : hops) {
                    mapController.addOrUpdate(hop);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    static class Ping implements Runnable {
        MapController mapController;
        InetAddress inetAddress;

        public Ping(MapController mapController, String address) {
            this.mapController = mapController;
            try {
                this.inetAddress = InetAddress.getByName(address);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        @Override
        public void run() {
            try {
                boolean reachable = inetAddress.isReachable(1000);

                if(reachable) {
                    mapController.addOrUpdate(inetAddress.getHostAddress());
                }
            } catch (Exception e) {
                System.out.println(e);
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

    public static List<String> cmdWindowsTraceroute(String ipAddress, int timeout, int maxHops) throws IOException {
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

                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return cmdResult;
    }

    public static List<String> cmdLinuxTraceroute(String ipAddress, int timeout, int maxHops) throws IOException {
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
                System.out.println(splited[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return cmdResult;
    }
}
