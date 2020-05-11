package com.foloke.cascade.Entities;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;

public class Device extends Entity {
    Image image;
    ArrayList<Port> ports;

    public Device(Image image) {
        this.image = image;
        ports = new ArrayList<>();
    }

    @Override
    public void render(GraphicsContext context) {
        context.drawImage(image, x, y);
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
            }
        } catch (SocketException e) {
            System.out.println(e);
        }
    }

    public void addPort(String address) {
        ports.add(new Port(this, address, ports.size()));
    }

    @Override
    protected void updatePosition() {
        super.updatePosition();
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

    public ArrayList<Port> getPorts() {
        return ports;
    }

    public static class Port extends Entity {
        Device parent;
        int position;
        String mac;
        public String address;
        public Port(Device parent, NetworkInterface networkInterface, int position) {
            this.parent = parent;
            this.position = position;
            rectangle = new Rectangle(8, 8);
            try {
                byte[] bytes = networkInterface.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    sb.append(String.format("%02X%s", bytes[i], (i < bytes.length - 1) ? "-" : ""));
                }
                mac = sb.toString();
                System.out.println(mac);
            } catch (SocketException e) {
                System.out.println(e);
            }
            address = networkInterface.getInetAddresses().nextElement().getHostAddress();
            updatePosition();
        }

        public Port(Device parent, String address, int position) {
            this.parent = parent;
            this.position = position;
            rectangle = new Rectangle(8, 8);
            mac = "";
            this.address = address;

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
            graphicsContext.strokeText(mac, rectangle.getX(),
                    rectangle.getY() + rectangle.getHeight());
            graphicsContext.strokeText(address, rectangle.getX(),
                    rectangle.getY() + rectangle.getHeight() + 4);
        }
    }
}
