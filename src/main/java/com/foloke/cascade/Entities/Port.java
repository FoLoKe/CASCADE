package com.foloke.cascade.Entities;

import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.ScanUtils;
import com.foloke.cascade.utils.Timer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.apache.commons.net.util.SubnetUtils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Port extends Entity {
    public enum AddType {AUTO, MANUAL, SNMP}
    public enum State {UP, DOWN, TESTING, UNKNOWN, DORMANT, NOT_PRESENT, LOWER_LAYER_DOWN}
    public Device parent;
    public State state = State.DOWN;
    public boolean pinging;
    int position;

    public int index;
    public String mac;
    public List<String> addresses = new ArrayList<>();
    public String primaryAddress;
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

        List<InterfaceAddress> addressesList = networkInterface.getInterfaceAddresses();
        primaryAddress = addressesList.get(0).getAddress().getHostAddress();
        for (InterfaceAddress interfaceAddress : addressesList) {
            addresses.add(interfaceAddress.getAddress().getHostAddress());
        }

        this.name = "auto_added";
        init(parent, position);
    }

    public Port(Device parent, String address, int position) {
        super(parent.mapController);
        rectangle = new Rectangle(8, 8);
        this.mac = "none";
        this.primaryAddress = address;
        this.addresses.add(address);
        this.name = "auto_added";
        init(parent, position);
    }

    public Port(Device parent, String[] params) {
        super(parent.mapController, params);
        init(parent, position = Integer.parseInt(params[7]));
        state = State.valueOf(params[5]);
        pinging = Boolean.parseBoolean(params[6]);
        mac = params[8];
        String addressesLine = params[9];
        String[] addressesArray = addressesLine.split("&");
        addresses = List.of(addressesArray);
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
            graphicsContext.strokeText(mac, rectangle.getX(),
                    rectangle.getY() + rectangle.getHeight() + 8);
            for (int i = 0; i < addresses.size(); i++) {
                graphicsContext.strokeText(addresses.get(i), rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight() + 12 + 4 * i);
            }
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

    @Override
    public String getSave() {
        StringBuilder addressesSave = new StringBuilder();
        addressesSave.append(addresses.get(0));
        for (int i = 1; i < addresses.size(); i++) {
            addressesSave.append("&").append(addresses.get(i));
        }
        return "PORT " + super.getSave()
                + " " + state.toString()
                + " " + pinging
                + " " + position
                + " " + mac
                + " " + addressesSave
                + " " + mask
                + " " + index
                + " " + addType;
    }
}

