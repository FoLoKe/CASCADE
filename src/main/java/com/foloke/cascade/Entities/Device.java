package com.foloke.cascade.Entities;

import com.foloke.cascade.Application;
import com.foloke.cascade.Components.*;
import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.Led;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.PortGroup;
import com.foloke.cascade.utils.Sprite;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
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

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@jakarta.persistence.Entity
@Table(name = "device")
public class Device extends Entity {
    public Led flowLed = new Led(this, Color.BLUE, 11, 7, 1, 1);
    public Led alarmLed = new Led(this, Color.RED, 12, 7, 1, 1);

    @Transient
    Sprite sprite;

    PortGroup ports = new PortGroup(mapController, this);

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

    public Device(MapController mapController) {
        super(mapController);
    }

    public Device(MapController mapController, String defaultIp) {
        super(mapController);
        sprite = Sprite.create(Application.spriteSheet, 0, 0, 16, 16, 1);
        children.add(ports);
        ports.setLocalPosition(0, 16);

        try {
            this.primaryIp = ByteBuffer.wrap(InetAddress.getByName(defaultIp).getAddress()).getInt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        addPort(defaultIp);
        updateSnmpConfiguration(snmpAddress);

        LogUtils.logToFile(name, "device created");
    }

    @Override
    public void render(GraphicsContext gc) {
        sprite.render(gc, getX(), getY());
        flowLed.render(gc);
        alarmLed.render(gc);

        if(showName) {
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(4));
            gc.fillText(name, getX(), getY());
        }

        ports.render(gc);
    }

    @Override
    public void tick(long timestamp) {
        super.tick(timestamp);
    }

    public Port addPort(String address) {
        Port port = new Port(this, address);

        addPort(port);
        LogUtils.logToFile(name, port.name + " : " + port.primaryAddress + " port added");

        return port;
    }

    public void addPort(Port port) {
        ports.add(port);

        if (ports.children.size() == 1) {
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
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
    }

    public Port pickPort(Point2D point2D) {
        return (Port) ports.hit(point2D);
    }

    public List<Port> getPorts() {
        List<Port> ports = new ArrayList<>();
        for (Entity child : children) {
            if (child instanceof Port)
                ports.add((Port)child);
        }
        return ports;
    }

    public Port addOrUpdatePort(Port port) {
        ports.addOrUpdatePort(port);

        return port;
    }

    @Override
    public void destroy() {
        super.destroy();
        ports.destroy();

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
        return ports.findPort(address);
    }

    //TODO: MAKE IT JSON OR DATABASE SAVE

    public static com.badlogic.ashley.core.Entity instance(double x, double y) {
        com.badlogic.ashley.core.Entity device = new com.badlogic.ashley.core.Entity();
        device.add(new PositionComponent(x, y));
        device.add(new VelocityComponent());
        device.add(new SpriteComponent(Sprite.create(Application.spriteSheet, 0, 0, 16, 16, 1)));
        device.add(new CollisionComponent());

        return device;
    }
}
