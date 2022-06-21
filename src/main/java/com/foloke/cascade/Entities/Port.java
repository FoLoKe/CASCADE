package com.foloke.cascade.Entities;

import com.foloke.cascade.Application;
import com.foloke.cascade.Components.CollisionComponent;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.SpriteComponent;
import com.foloke.cascade.Components.Tags.PortTag;
import com.foloke.cascade.Components.VelocityComponent;
import com.foloke.cascade.utils.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.apache.commons.net.util.SubnetUtils;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Port extends Entity {
    Sprite sprite;
    Led led = new Led(this, Color.RED, 2.5, 1.25, 3, 0.75);

    public static com.badlogic.ashley.core.Entity instance(double x, double y) {
        com.badlogic.ashley.core.Entity port = new com.badlogic.ashley.core.Entity();
        port.add(new PositionComponent(x, y));
        port.add(new VelocityComponent());
        port.add(new SpriteComponent(Sprite.create(Application.spriteSheet, 16, 0, 8, 8, 1)));
        port.add(new CollisionComponent(8, 8));
        port.add(new PortTag());
        return port;
    }

    public enum AddType {AUTO, MANUAL, SNMP}
    public enum State {UP, DOWN, TESTING, UNKNOWN, DORMANT, NOT_PRESENT, LOWER_LAYER_DOWN}
    public Device parent;
    public State state = State.DOWN;
    public boolean pinging;

    public int index;
    public String mac;
    public List<String> addresses = new ArrayList<>();
    public String primaryAddress;
    public int mask = 24;

    public ArrayList<Cable.Connector> connectors = new ArrayList<>();
    public AddType addType;

    private Port(Device parent) {
        super(parent.mapController);
        this.sprite = Sprite.create(Application.spriteSheet, 16, 0, 8, 8, 1);
        led.activate();
    }

    public Port(Device parent, NetworkInterface networkInterface) {
        this(parent);
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

        List<InterfaceAddress> addressesList = networkInterface.getInterfaceAddresses();
        primaryAddress = addressesList.get(0).getAddress().getHostAddress();
        for (InterfaceAddress interfaceAddress : addressesList) {
            addresses.add(interfaceAddress.getAddress().getHostAddress());
        }

        this.name = "auto_added";
        init(parent);
    }

    public Port(Device parent, String address) {
        this(parent);
        hitBox = new Rectangle(8, 8);
        this.mac = "none";
        this.primaryAddress = address;
        this.addresses.add(address);
        this.name = "auto_added";
        init(parent);
    }

    private void init(Device parent) {
        this.parent = parent;
        this.addType = AddType.AUTO;

        this.hitBox = new Rectangle(8, 8);
        addTask(new Timer(1000000000) {
            @Override
            public void execute() {
                if(pinging) {
                    ScanUtils.ping(Port.this);
                }
            }
        });
    }

    public void render(GraphicsContext gc) {
        Color stateColor;
        switch (state) {
            case UP:
                stateColor = Color.GREEN;
                break;
            case TESTING:
                stateColor = Color.YELLOW;
                break;
            case UNKNOWN:
                stateColor = Color.BLACK;
                break;
            case NOT_PRESENT:
                stateColor = Color.GRAY;
                break;
            case DORMANT:
                stateColor = Color.CYAN;
                break;
            case LOWER_LAYER_DOWN:
                stateColor = Color.BLUE;
                break;
            default:
                stateColor = Color.RED;
        }

        sprite.setPosition(hitBox.getX(), hitBox.getY());
        sprite.render(gc);

        led.setColor(stateColor);
        led.render(gc);

        if (selected) {
            double offsetX = hitBox.getX();
            double offsetY = hitBox.getY() + hitBox.getHeight();
            gc.setFill(Color.BLACK);
            gc.setFont(new Font("sans", 3));
            gc.fillText(name, offsetX,
                    offsetY + 4);
            gc.fillText(mac, offsetX,
                    offsetY + 8);

            gc.fillText(primaryAddress, offsetX,
                    offsetY + 12);

            for (int i = 0; i < addresses.size(); i++) {
                gc.fillText(addresses.get(i), offsetX,
                        offsetY + 16 + 4 * i);
            }
        }
    }

    @Override
    public Entity hit(Point2D point2D) {
        if (hitBox.contains(point2D)) {
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
            connector.destroy();
            iterator.remove();
        }
    }

    @Override
    public String toString() {
        return name + " " + this.index;
    }

    public boolean isInRange(String toCheck) {
        for(String address : addresses) {
            if (address.length() > 0) {
                SubnetUtils subnetUtils = new SubnetUtils(address + "/" + mask);
                if (subnetUtils.getInfo().isInRange(toCheck))
                    return true;
            }
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
}

