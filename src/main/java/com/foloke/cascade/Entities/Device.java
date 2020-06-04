package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.ScanUtils;
import com.foloke.cascade.utils.Timer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;

public class Device extends Entity {
    Image image;
    ArrayList<Port> ports;
    public Target<UdpAddress> target;
    public UsmUser user;

    String name = "name";

    String snmpAddress = "0.0.0.0";
    String snmpPort = "161";
    int snmpVersion = SnmpConstants.version3;
    int snmpTimeout = 1000;
    String snmpName = "publics";


    //SNMPv3
    String snmpPassword = "12345678";
    OID authProtocol = AuthMD5.ID;
    OID encryptionProtocol = PrivDES.ID;
    String snmpEncryptionPass = "12345678";
    int securityLevel = SecurityLevel.NOAUTH_NOPRIV;

    public Device(Image image, MapController mapController) {
        super(mapController);
        this.image = image;
        ports = new ArrayList<>();
        target = new CommunityTarget<>();
    }

    @Override
    public void render(GraphicsContext context) {
        context.drawImage(image, rectangle.getX(), rectangle.getY());
        for(Port port : ports) {
            port.render(context);
        }
    }

    @Override
    public void tick(long timestamp) {
        super.tick(timestamp);
        for (Port port : ports) {
            port.tick(timestamp);
        }
    }

    public Port addPort(NetworkInterface networkInterface) {
        try {
            if(networkInterface.getHardwareAddress() != null) {
                Port port = new Port(this, networkInterface, ports.size());
                ports.add(port);
                if(ports.size() == 1) {
                    updateSnmpConfiguration(networkInterface.getInetAddresses().nextElement().getHostAddress());
                }

                return port;
            }
        } catch (SocketException e) {
            LogUtils.log(e.toString());
        }

        return null;
    }

    public Port addPort(String address) {
        Port port = new Port(this, address, ports.size());

        ports.add(port);
        if(ports.size() == 1) {
            updateSnmpConfiguration(address);
        }

        LogUtils.logToFile(name, "port added");

        return port;
    }

    public void updateSnmpConfiguration(String snmpAddress) {
        this.snmpAddress = snmpAddress;
        if(snmpVersion != SnmpConstants.version3) {
            target = new CommunityTarget<>();
            ((CommunityTarget<UdpAddress>)target).setCommunity(new OctetString(snmpName));
            target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
        } else {
            user = new UsmUser(new OctetString(snmpName),
                    authProtocol, new OctetString(snmpPassword),
                    encryptionProtocol, new OctetString(snmpEncryptionPass));

            target = new UserTarget<>();
            target.setSecurityLevel(securityLevel);
        }

        target.setVersion(snmpVersion);
        target.setAddress(new UdpAddress(snmpAddress + "/" + snmpPort));
        target.setTimeout(snmpTimeout);
        target.setSecurityName(new OctetString(snmpName));



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

    public Port addOrUpdatePort(Port port) {
        for (Port existingPort : ports) {
            if (port.id == existingPort.id ||
                    existingPort.addType == Port.AddType.SNMP ||
                    existingPort.addType == Port.AddType.AUTO) {
                if(existingPort.mac.length() > 0 && existingPort.mac.equals(port.mac) ||
                        existingPort.address.length() > 0 &&existingPort.address.equals(port.address)) {
                    updatePort(existingPort, port);
                    return existingPort;
                }
            }
        }

        port.position = ports.size();
        ports.add(port);

        return port;
    }

    private void updatePort(Port existingPort, Port port) {
        existingPort.addType = Port.AddType.SNMP;
        existingPort.name = port.name;
        existingPort.address = port.address;
        existingPort.mac = port.mac;
    }

    @Override
    public void destroy() {
        super.destroy();
        for (Port port : ports) {
            port.destroy();
        }
    }

    public OID getAuthProtocol() {
        return authProtocol;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }

    public void setAuthProtocol(OID authProtocol) {
        this.authProtocol = authProtocol;
    }

    public OID getEncryptionProtocol() {
        return encryptionProtocol;
    }

    public void setEncryptionProtocol(OID encryptionProtocol) {
        this.encryptionProtocol = encryptionProtocol;
    }

    public String getSnmpEncryptionPass() {
        return snmpEncryptionPass;
    }

    public void setSnmpEncryptionPass(String snmpEncryptionPass) {
        this.snmpEncryptionPass = snmpEncryptionPass;
    }

    public String getSnmpPassword() {
        return snmpPassword;
    }

    public void setSnmpPassword(String snmpPassword) {
        this.snmpPassword = snmpPassword;
    }

    public String getSnmpAddress() {
        return snmpAddress;
    }

    public void setSnmpAddress(String snmpAddress) {
        this.snmpAddress = snmpAddress;
    }

    public String getSnmpPort() {
        return snmpPort;
    }

    public void setSnmpPort(String snmpPort) {
        this.snmpPort = snmpPort;
    }

    public int getSnmpVersion() {
        return snmpVersion;
    }

    public void setSnmpVersion(int snmpVersion) {
        this.snmpVersion = snmpVersion;
    }

    public int getSnmpTimeout() {
        return snmpTimeout;
    }

    public void setSnmpTimeout(int snmpTimeout) {
        this.snmpTimeout = snmpTimeout;
    }

    public String getSnmpCommunity() {
        return snmpName;
    }

    public void setSnmpCommunity(String snmpCommunity) {
        this.snmpName = snmpCommunity;
    }

    public Port findPort(String address) {
        for (Port port : ports) {
            if(port.address.equals(address)) {
                return port;
            }
        }

        return ports.get(0);
    }

    public static class Port extends Entity {
        public enum AddType {AUTO, MANUAL, SNMP}
        public Device parent;
        public boolean active;
        public boolean pinging;
        int position;

        public int id;
        public String mac;
        public String name;
        public String address;

        public ArrayList<Cable.Connector> connectors = new ArrayList<>();
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

            } catch (SocketException e) {
                LogUtils.log(e.toString());
            }

            this.address = networkInterface.getInetAddresses().nextElement().getHostAddress();
            this.addType = AddType.AUTO;
            this.name = "auto added";

            addTask(new Timer(1000000000) {
                @Override
                public void execute() {
                    if(pinging) {
                        ScanUtils.ping(Port.this);
                    }
                }
            });

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

            addTask(new Timer(1000000000) {
                @Override
                public void execute() {
                    if(pinging) {
                        ScanUtils.ping(Port.this);
                    }
                }
            });

            updatePosition();
        }

        public void updatePosition() {
            rectangle.setX(parent.getX() + position * rectangle.getWidth());
            rectangle.setY(parent.getY() + parent.getHitBox().getHeight());
        }

        public void render(GraphicsContext graphicsContext) {
            if(active) {
                graphicsContext.setStroke(Color.GREEN);
            } else {
                graphicsContext.setStroke(Color.RED);
            }

            graphicsContext.setLineWidth(0.2f);
            graphicsContext.strokeRect(rectangle.getX(),
                    rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());


            if (selected) {
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setFont(new Font("sans", 2));
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
            if(connectors.size() > 0) {
                return connectors.get(0);
            }

            return null;
        }

        public boolean isConnectedTo(Port port) {
            for (Cable.Connector connector : connectors) {
                if(connector.getParent().connectorA.connection == port ||
                        connector.getParent().connectorB.connection == port) {
                    return true;
                }
            }

            return false;
        }

        public void connect(Cable.Connector connector) {
            connectors.add(connector);
        }

        public void disconnect(Cable.Connector connector) {
            connectors.removeIf(myConnector -> myConnector == connector);
        }

        @Override
        public void destroy() {
            super.destroy();
            for (Cable.Connector connector : connectors) {
                connector.disconnect();
            }
            connectors.clear();
        }

        @Override
        public String toString() {
            return name + " " + this.address;
        }
    }
}
