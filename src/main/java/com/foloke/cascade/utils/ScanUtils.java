package com.foloke.cascade.utils;

import com.foloke.cascade.Controllers.MapController;
import org.apache.commons.net.util.SubnetUtils;

import java.net.InetAddress;

public class ScanUtils {
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
                System.out.println(inetAddress.toString() + " " + reachable);

                if(reachable) {
                    mapController.addOrUpdate(inetAddress.getHostAddress());
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
