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
import org.apache.commons.net.util.SubnetUtils;
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
import java.util.Iterator;

public class Device extends Entity {
    Image image;
    ArrayList<Port> ports;
    public Target<UdpAddress> target;
    public UsmUser user;

    String snmpAddress = "0.0.0.0";
    String snmpPort = "161";
    int snmpVersion = SnmpConstants.version2c;
    int snmpTimeout = 5000;
    String snmpName = "public";

    //SNMPv3
    String snmpPassword = "12345678";
    OID authProtocol = AuthMD5.ID;
    OID encryptionProtocol = PrivDES.ID;
    String snmpEncryptionPass = "12345678";
    int securityLevel = SecurityLevel.NOAUTH_NOPRIV;

    public Device(Image image, MapController mapController) {
        super(mapController);
        init(image);
        LogUtils.logToFile(name, "device created");
        updateSnmpConfiguration(snmpAddress);
    }

    public Device(Image image, MapController mapController, String[] params) {
        super(mapController, params);
        init(image);
        snmpAddress = params[5];
        snmpPort = params[6];
        snmpVersion = Integer.parseInt(params[7]);
        snmpTimeout = Integer.parseInt(params[8]);
        snmpName = params[9];
        snmpPassword = params[10];
        authProtocol = new OID(params[11]);
        encryptionProtocol = new OID(params[12]);
        snmpEncryptionPass = params[13];
        securityLevel = Integer.parseInt(params[14]);
        updateSnmpConfiguration(snmpAddress);

        setLocation(Double.parseDouble(params[3]), Double.parseDouble(params[4]));

        LogUtils.logToFile(name, "device loaded");
    }

    private void init(Image image) {
        this.image = image;
        ports = new ArrayList<>();
    }

    @Override
    public void render(GraphicsContext context) {
        context.drawImage(image, rectangle.getX(), rectangle.getY());
        for (Port port : ports) {
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
            if (networkInterface.getHardwareAddress() != null) {
                Port port = new Port(this, networkInterface, ports.size());
                addPort(port);
                return port;
            }
        } catch (SocketException e) {
            LogUtils.log(e.toString());
            LogUtils.logToFile(name, e.toString());
        }

        return null;
    }

    public Port addPort(String address) {
        Port port = new Port(this, address, ports.size());

        addPort(port);

        LogUtils.logToFile(name, port.name + " : " + port.address + " port added");

        return port;
    }

    public void addPort(Port port) {
        ports.add(port);

        if (ports.size() == 1) {
            updateSnmpConfiguration(port.address);
            LogUtils.logToFile(name, port.name + " is only and has sat SNMP defaults");
        }
        LogUtils.logToFile(name, port.name + " : " + port.address + " port added");
    }

    public void updateSnmpConfiguration(String snmpAddress) {
        LogUtils.logToFile(name, "updating SNMP config");
        this.snmpAddress = snmpAddress;
        if (snmpVersion != SnmpConstants.version3) {
            target = new CommunityTarget<>();
            ((CommunityTarget<UdpAddress>) target).setCommunity(new OctetString(snmpName));
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

        LogUtils.logToFile(name, "SNMP config: \n" +
                "Address " + snmpAddress + "\n" +
                "Community/Username " + snmpName + "\n" +
                "Timeout " + snmpTimeout + "\n" +
                "Version " + snmpVersion + "\n" +
                "Level " + securityLevel + "\n" +
                "Encryption protocol " + encryptionProtocol + "\n" +
                "Auth protocol " + authProtocol + "\n" +
                "Auth pass " + snmpPassword + "\n" +
                "Encryption pass " + snmpEncryptionPass);
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
            if (port.rectangle.contains(point2D.getX(), point2D.getY())) {
                LogUtils.logToFile(name, "port picked " + port.name + " : " + port.address);
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
            if (port.index == existingPort.index ||
                    existingPort.addType == Port.AddType.SNMP ||
                    existingPort.addType == Port.AddType.AUTO) {
                if (existingPort.mac.length() > 0 && existingPort.mac.equals(port.mac)) {
                    updatePort(existingPort, port);
                    LogUtils.logToFile(name, "port found and updated " + port.name + " : " + port.address);
                    return existingPort;
                } else if (existingPort.address.length() > 0 && port.address.length() > 0) {
                    SubnetUtils subnetUtils = new SubnetUtils(existingPort.address + "/" + existingPort.mask);
                    if (existingPort.address.equals(port.address) || subnetUtils.getInfo().isInRange(port.address)) {
                        updatePort(existingPort, port);
                        LogUtils.logToFile(name, "port found and updated " + port.name + " : " + port.address);
                        return existingPort;
                    }
                }
            }
        }

        port.position = ports.size();
        addPort(port);
        port.updatePosition();
        LogUtils.logToFile(name, "port not found and added " + port.name + " : " + port.address);

        return port;
    }

    private void updatePort(Port existingPort, Port port) {
        existingPort.addType = Port.AddType.SNMP;
        existingPort.name = port.name;
        existingPort.address = port.address;
        existingPort.mac = port.mac;

        LogUtils.logToFile(name, existingPort + " port updated");
    }

    @Override
    public void destroy() {
        super.destroy();
        for (Port port : ports) {
            port.destroy();
        }
        LogUtils.logToFile(name, this + " destroyed");
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

    @Override
    public String getSave() {
        String saveString = "DEVICE " + super.getSave() +
                " " + snmpAddress +
                " " + snmpPort +
                " " + snmpVersion +
                " " + snmpTimeout +
                " " + snmpName +
                " " + snmpPassword +
                " " + authProtocol +
                " " + encryptionProtocol +
                " " + snmpEncryptionPass +
                " " + securityLevel;

        StringBuilder stringBuilder = new StringBuilder();

        for (Port port: ports) {
            stringBuilder.append("\n").append(port.getSave());
        }
        saveString += stringBuilder.toString();

        return saveString;
    }

    public static class Port extends Entity {
        public enum AddType {AUTO, MANUAL, SNMP}
        public enum State {UP, DOWN, TESTING, UNKNOWN, DORMANT, NOT_PRESENT, LOWER_LAYER_DOWN}
        public Device parent;
        public State state = State.DOWN;
        public boolean pinging;
        int position;

        public int index;
        public String mac;
        public String address;
        public int mask = 24;

        public ArrayList<Cable.Connector> connectors = new ArrayList<>();
        public AddType addType;

        public Port(Device parent, NetworkInterface networkInterface, int position) {
            super(parent.mapController);
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
            this.name = "auto_added";
            init(parent, position);
        }

        public Port(Device parent, String address, int position) {
            super(parent.mapController);
            rectangle = new Rectangle(8, 8);
            this.mac = "none";
            this.address = address;
            this.name = "auto_added";
            init(parent, position);
        }

        public Port(Device parent, String[] params) {
            super(parent.mapController, params);
            init(parent, position = Integer.parseInt(params[7]));
            state = State.valueOf(params[5]);
            pinging = Boolean.parseBoolean(params[6]);
            mac = params[8];
            address = params[9];
            mask = Integer.parseInt(params[10]);
            index = Integer.parseInt(params[11]);
            addType = AddType.valueOf(params[12]);
        }

        private void init(Device parent, int position) {
            this.position = position;
            this.parent = parent;
            this.addType = AddType.AUTO;
            
            this.rectangle = new Rectangle(8, 8);
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
            switch (state) {
                case UP:
                    graphicsContext.setStroke(Color.GREEN);
                    break;
                case DOWN:
                    graphicsContext.setStroke(Color.RED);
                    break;
                case TESTING:
                    graphicsContext.setStroke(Color.YELLOW);
                    break;
                case UNKNOWN:
                    graphicsContext.setStroke(Color.BLACK);
                    break;
                case NOT_PRESENT:
                    graphicsContext.setStroke(Color.GRAY);
                    break;
                case DORMANT:
                    graphicsContext.setStroke(Color.CYAN);
                    break;
                case LOWER_LAYER_DOWN:
                    graphicsContext.setStroke(Color.BLUE);
                    break;
            }

            graphicsContext.setLineWidth(0.2f);
            graphicsContext.strokeRect(rectangle.getX(),
                    rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());


            if (selected) {
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setFont(new Font("sans", 2));
                graphicsContext.strokeText(name, rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight() + 4);
                graphicsContext.strokeText(address, rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight() + 8);
                graphicsContext.strokeText(mac, rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight() + 12);
            }
        }

        @Override
        public Entity hit(Point2D point2D) {
            if (rectangle.contains(point2D)) {
                if(connectors.size() > 0) {
                    if(connectors.get(0).getHitBox().contains(point2D)) {
                        return connectors.get(0);
                    }
                } else {
                    return this;
                }
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
            Iterator<Cable.Connector> iterator = connectors.iterator();
            while (iterator.hasNext()) {
                Cable.Connector connector = iterator.next();
                connector.connection = null;
                iterator.remove();
            }
        }

        @Override
        public String toString() {
            return name + " " + this.address;
        }

        public boolean isInRange(String address) {
            if(this.address.length() > 0) {
                SubnetUtils subnetUtils = new SubnetUtils(this.address + "/" + mask);
                return subnetUtils.getInfo().isInRange(address);
            }

            return false;
        }

        public void setState(State state) {
            this.state = state;
        }

        public void setState(int i) {
            State[] stateList = State.values();
            state = stateList[i - 1];
        }

        @Override
        public String getSave() {
            String saveString = "PORT " + super.getSave()
                    + " " + state.toString()
                    + " " + pinging
                    + " " + position
                    + " " + mac
                    + " " + address
                    + " " + mask
                    + " " + index
                    + " " + addType;

            return saveString;
        }
    }
}
