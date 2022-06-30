package com.foloke.cascade.utils;

import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
import com.foloke.cascade.Entities.Port;
import javafx.scene.canvas.GraphicsContext;
import org.apache.commons.net.util.SubnetUtils;

public class PortGroup extends Entity {
    public Device parent;

    public PortGroup(Device device) {
        super();
        this.parent = device;
    }

    public void render(GraphicsContext gc) {
        //for (Port port : ports) {
        //    port.render(gc);
        //}
    }

    public void tick(long timestamp) {
//        Iterator<Port> portIterator = ports.iterator();
//        while (portIterator.hasNext()) {
//            Port port = portIterator.next();
//            port.tick(timestamp);
//            if(port.destroyed) {
//                port.cleanup();
//                portIterator.remove();
//                recalculatePortsPositions();
//            }
//        }
    }

    private void recalculatePortsPositions() {
//        for (Port port : ports) {
//
//        }
    }

    public Port addOrUpdatePort(Port port) {
        for (Entity child : children) {
            if(!(child instanceof Port))
                continue;
            Port existingPort = (Port) child;
            if (port.index == existingPort.index ||
                    existingPort.addType == Port.AddType.SNMP ||
                    existingPort.addType == Port.AddType.AUTO) {
                if (existingPort.mac.length() > 0 && existingPort.mac.equals(port.mac)) {
                    updatePort(existingPort, port);
                    LogUtils.logToFile(parent.getName(), "port found and updated " + port.getName() + " : " + port.primaryAddress);
                    return existingPort;
                } else if (existingPort.primaryAddress.length() > 0 && port.primaryAddress.length() > 0) {
                    SubnetUtils subnetUtils = new SubnetUtils(existingPort.primaryAddress + "/" + existingPort.mask);
                    if (existingPort.primaryAddress.equals(port.primaryAddress) || subnetUtils.getInfo().isInRange(port.primaryAddress)) {
                        updatePort(existingPort, port);
                        LogUtils.logToFile(parent.getName(), "port found and updated " + port.getName() + " : " + port.primaryAddress);
                        return existingPort;
                    }
                }
            }
        }

        add(port);
        recalculatePortsPositions();
        LogUtils.logToFile(parent.getName(), "port not found and added " + port.getName() + " : " + port.primaryAddress);

        return port;
    }

    private void updatePort(Port existingPort, Port port) {
        existingPort.addType = Port.AddType.SNMP;
        existingPort.setName(port.getName());
        existingPort.primaryAddress = port.primaryAddress;
        existingPort.mac = port.mac;

        LogUtils.logToFile(parent.getName(), existingPort + " port updated");
    }

    public Port findPort(String address) {
        for (Entity child : children) {
            if (!(child instanceof Port))
                continue;
            Port port = (Port) child;

            for(String portAddress : port.addresses) {
                if (portAddress.equals(address))
                    return port;
            }
        }

        return null;
    }
}
