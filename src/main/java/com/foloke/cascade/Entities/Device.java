package com.foloke.cascade.Entities;

import com.foloke.cascade.Application;
import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.LogUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
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

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@jakarta.persistence.Entity
@Table(name = "device")
public class Device extends Entity {
    @Transient
    Image image = Application.image;
    @Transient
    List<Port> ports = Collections.synchronizedList(new ArrayList<>());

    @Id
    @Column(name = "primaryIp")
    public int primaryIp;

    public boolean showName;

    //SNMP
    @Transient
    public Target<UdpAddress> target;
    @Transient
    public UsmUser user;
    String snmpAddress = "127.0.0.1";
    String snmpPort = "161";
    int snmpVersion = SnmpConstants.version2c;
    int snmpTimeout = 5000;
    String snmpName = "public";

    //SNMPv3
    String snmpPassword = "12345678";
    @Transient
    OID authProtocol = AuthMD5.ID;
    @Transient
    OID encryptionProtocol = PrivDES.ID;
    String snmpEncryptionPass = "12345678";
    int securityLevel = SecurityLevel.NOAUTH_NOPRIV;

    public Device(MapController mapController, String defaultIp) {
        super(mapController);

        try {
            this.primaryIp = ByteBuffer.wrap(InetAddress.getByName(defaultIp).getAddress()).getInt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        addPort(defaultIp);
        updateSnmpConfiguration(snmpAddress);

        LogUtils.logToFile(name, "device created");
    }

    public Device(MapController mapController, String[] params) {
        super(mapController, params);
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

        primaryIp = Integer.parseInt(params[15]);

        setLocation(Double.parseDouble(params[3]), Double.parseDouble(params[4]));

        LogUtils.logToFile(name, "device loaded");
    }

    @Override
    public void render(GraphicsContext context) {
        context.drawImage(image, rectangle.getX(), rectangle.getY());
        if(showName) {
            context.setFill(Color.BLACK);
            context.setFont(new Font(4));
            context.fillText(name, rectangle.getX(), rectangle.getY());
        }
        for (Port port : ports) {
            port.render(context);
        }
    }

    @Override
    public void tick(long timestamp) {
        super.tick(timestamp);

        Iterator<Port> portIterator = ports.iterator();
        while (portIterator.hasNext()) {
            Port port = portIterator.next();
            port.tick(timestamp);
            if(port.destroyed) {
                port.cleanup();
                portIterator.remove();
                recalculatePortsPositions();
            }
        }
    }

    private void recalculatePortsPositions() {
        int position = 0;
        for (Port port : ports) {
            port.position = position++;
            port.updatePosition();
        }
    }

    public Port addPort(String address) {
        Port port = new Port(this, address, ports.size());

        addPort(port);
        LogUtils.logToFile(name, port.name + " : " + port.primaryAddress + " port added");

        return port;
    }

    public void addPort(Port port) {
        ports.add(port);

        if (ports.size() == 1) {
            updateSnmpConfiguration(port.primaryAddress);

            LogUtils.logToFile(name, port.name + " is only and has sat SNMP defaults");
        }
        LogUtils.logToFile(name, port.name + " : " + port.primaryAddress + " port added");
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
                LogUtils.logToFile(name, "port picked " + port.name + " : " + port.primaryAddress);
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

    public List<Port> getPorts() {
        return ports;
    }

    public Port addOrUpdatePort(Port port) {
        for (Port existingPort : ports) {
            if (port.index == existingPort.index ||
                    existingPort.addType == Port.AddType.SNMP ||
                    existingPort.addType == Port.AddType.AUTO) {
                if (existingPort.mac.length() > 0 && existingPort.mac.equals(port.mac)) {
                    updatePort(existingPort, port);
                    LogUtils.logToFile(name, "port found and updated " + port.name + " : " + port.primaryAddress);
                    return existingPort;
                } else if (existingPort.primaryAddress.length() > 0 && port.primaryAddress.length() > 0) {
                    SubnetUtils subnetUtils = new SubnetUtils(existingPort.primaryAddress + "/" + existingPort.mask);
                    if (existingPort.primaryAddress.equals(port.primaryAddress) || subnetUtils.getInfo().isInRange(port.primaryAddress)) {
                        updatePort(existingPort, port);
                        LogUtils.logToFile(name, "port found and updated " + port.name + " : " + port.primaryAddress);
                        return existingPort;
                    }
                }
            }
        }

        port.position = ports.size();
        addPort(port);
        port.updatePosition();
        LogUtils.logToFile(name, "port not found and added " + port.name + " : " + port.primaryAddress);

        return port;
    }

    private void updatePort(Port existingPort, Port port) {
        existingPort.addType = Port.AddType.SNMP;
        existingPort.name = port.name;
        existingPort.primaryAddress = port.primaryAddress;
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
            for(String portAddress : port.addresses) {
                if (portAddress.equals(address))
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
                " " + securityLevel +
                " " + primaryIp;

        StringBuilder stringBuilder = new StringBuilder();

        for (Port port: ports) {
            stringBuilder.append("\n").append(port.getSave());
        }
        saveString += stringBuilder.toString();

        return saveString;
    }
}
