package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.LogUtils;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.snmp4j.CommunityTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;

public class Device extends Entity {
    Image image;
    ArrayList<Port> ports;
    public CommunityTarget<UdpAddress> communityTarget;

    String name = "name";

    String snmpAddress = "0.0.0.0";
    String snmpPort = "161";
    int snmpVersion = SnmpConstants.version2c;
    int snmpTimeout = 20000;
    String snmpCommunity = "public";

    public Device(Image image, MapController mapController) {
        super(mapController);
        this.image = image;
        ports = new ArrayList<>();
        communityTarget = new CommunityTarget<>();
    }

    @Override
    public void render(GraphicsContext context) {
        context.drawImage(image, rectangle.getX(), rectangle.getY());
        for(Port port : ports) {
            port.render(context);
        }
    }

    @Override
    public void tick() {

    }

    public void addPort(NetworkInterface networkInterface) {
        try {
            if(networkInterface.getHardwareAddress() != null) {
                ports.add(new Port(this, networkInterface, ports.size()));
                if(ports.size() == 1) {
                    setCommunityDefaults(networkInterface.getInetAddresses().nextElement().getHostAddress());
                }
            }
        } catch (SocketException e) {
            LogUtils.log(e.toString());
        }
    }

    public void addPort(String address) {
        ports.add(new Port(this, address, ports.size()));
        if(ports.size() == 1) {
            setCommunityDefaults(address);
        }

        LogUtils.logToFile(name, "port added");
    }

    public void setCommunityDefaults(String snmpAddress) {
        this.snmpAddress = snmpAddress;
        communityTarget.setCommunity(new OctetString(snmpCommunity));
        communityTarget.setVersion(snmpVersion);
        communityTarget.setAddress(new UdpAddress(snmpAddress + "/" + snmpPort));
        communityTarget.setTimeout(snmpTimeout);
    }

    @Override
    public void setLocation(double x, double y) {
        super.setLocation(x, y);
        for (Port port : ports) {
            port.updatePosition();
        }
    }

    public Port pickPort(Point2D point2D) {
        for (Port port : ports) {
            if(port.rectangle.contains(point2D.getX(), point2D.getY())) {
                return port;
            }
        }
        return null;
    }

    @Override
    public Entity hit(Point2D point2D) {
        if (rectangle.contains(point2D)) {
            return this;
        }

        return null;
    }

    public ArrayList<Port> getPorts() {
        return ports;
    }

    public void addOrUpdatePort(Port port) {
        for (Port existingPort : ports) {
            if (port.id == existingPort.id ||
                    existingPort.addType == Port.AddType.SNMP ||
                    existingPort.addType == Port.AddType.AUTO) {
                if(existingPort.mac.length() > 0 && existingPort.mac.equals(port.mac) ||
                        existingPort.address.length() > 0 &&existingPort.address.equals(port.address)) {
                    updatePort(existingPort, port);
                    return;
                }
            }
        }

        port.position = ports.size();
        ports.add(port);
    }

    private void updatePort(Port existingPort, Port port) {
        existingPort.addType = Port.AddType.SNMP;
        existingPort.name = port.name;
        existingPort.address = port.address;
        existingPort.mac = port.mac;
    }

    public static class Port extends Entity {
        public enum AddType {AUTO, MANUAL, SNMP}
        public Device parent;
        int position;

        public int id;
        public String mac;
        public String name;
        public String address;

        Cable.Connector connector;
        public AddType addType;

        public Port(Device parent, NetworkInterface networkInterface, int position) {
            super(parent.mapController);
            this.parent = parent;
            this.position = position;
            this.rectangle = new Rectangle(8, 8);

            try {
                byte[] bytes = networkInterface.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    sb.append(String.format("%02X%s", bytes[i], (i < bytes.length - 1) ? "-" : ""));
                }

                this.mac = sb.toString();
                LogUtils.log(mac);
            } catch (SocketException e) {
                LogUtils.log(e.toString());
            }

            this.address = networkInterface.getInetAddresses().nextElement().getHostAddress();
            this.addType = AddType.AUTO;
            this.name = "auto added";
            updatePosition();
        }

        public Port(Device parent, String address, int position) {
            super(parent.mapController);
            this.parent = parent;
            this.position = position;
            rectangle = new Rectangle(8, 8);

            this.mac = "none";
            this.name = "auto added";
            this.address = address;
            this.addType = AddType.AUTO;

            updatePosition();
        }

        @Override
        public void tick() {

        }

        public void updatePosition() {
            rectangle.setX(parent.getX() + position * rectangle.getWidth());
            rectangle.setY(parent.getY() + parent.getHitBox().getHeight());
        }

        public void render(GraphicsContext graphicsContext) {
            graphicsContext.setLineWidth(0.2f);
            graphicsContext.setFont(new Font("sans", 2));
            graphicsContext.strokeRect(rectangle.getX(),
                    rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

            if (selected) {
                graphicsContext.strokeText(name, rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight());
                graphicsContext.strokeText(address, rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight() + 4);
                graphicsContext.strokeText(mac, rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight() + 8);
            }
        }

        @Override
        public Entity hit(Point2D point2D) {
            if (rectangle.contains(point2D)) {
                return this;
            }

            return null;
        }

        public Entity getObject() {
            if(connector == null) {
                return this;
            } else {
                return connector;
            }
        }
    }
}
