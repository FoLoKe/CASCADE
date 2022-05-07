package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.LogUtils;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Cable extends Entity {

    public Connector connectorA;
    public Connector connectorB;

    public Cable(MapController mapController) {
        super(mapController);
        connectorA = new Connector(this);
        connectorB = new Connector(this);
    }

    public Cable(MapController mapController, String[] params) {
        super(mapController, params);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.strokeLine(connectorA.getX() + connectorA.rectangle.getWidth() / 2,
                connectorA.getY() + connectorA.rectangle.getHeight() / 2,
                connectorB.getX() + connectorB.rectangle.getWidth() / 2,
                connectorB.getY() + connectorB.rectangle.getHeight() / 2);
        connectorA.render(gc);
        connectorB.render(gc);
    }

    @Override
    public void tick(long timestamp) {
        connectorA.tick(timestamp);
        connectorB.tick(timestamp);
    }

    @Override
    public Entity hit(Point2D point2D) {
        if (connectorA.hit(point2D) != null) {
            return connectorA;
        }

        if (connectorB.hit(point2D) != null) {
            return connectorB;
        }

        return null;
    }

    @Override
    public void destroy() {
        super.destroy();

        if(!connectorA.destroyed) {
            connectorA.destroy();
        }

        if (!connectorB.destroyed) {
            connectorB.destroy();
        }
    }

    @Override
    public String getSave() {
        return "CABLE " + super.getSave() +
                "\n" + connectorA.getSave() +
                "\n" + connectorB.getSave();
    }

    public static class Connector extends Entity {
        public Port connection;
        private long connectionID;
        private Cable parent;

        public Connector(Cable parent) {
            super(parent.mapController);
            init(parent);
        }

        public Connector(Cable parent, String[] params) {
            super(parent.mapController, params);
            connectionID = Integer.parseInt(params[5]);
            init(parent);
        }

        private void init(Cable parent) {
            this.parent = parent;
            rectangle = new Rectangle(5, 5);
        }

        @Override
        public void render(GraphicsContext gc) {
            gc.setStroke(Color.BLACK);
            gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        }

        @Override
        public void tick(long timestamp) {
            if(connection != null) {
                rectangle.setX(connection.getX());
                rectangle.setY(connection.getY());
            }
        }

        @Override
        public Entity hit(Point2D point2D) {
            if(rectangle.contains(point2D)) {
                return this;
            }
            return null;
        }

        public void connect(Port connection) {
            this.connection = connection;
            connectionID = connection.getID();
            connection.connect(this);
            LogUtils.log(connection.address + " connected with cable " + this);
        }

        public void disconnect() {
            if(connection != null) {
                connection.disconnect(this);
                connectionID = -1;
                LogUtils.log(connection.address + " disconnected with cable " + this);
                this.connection = null;
            }
        }

        public Cable getParent() {
            return parent;
        }

        @Override
        public void destroy() {
            super.destroy();
            disconnect();
            parent.destroy();
        }

        @Override
        public String getSave() {
            return "CONNECTOR " + super.getSave() + " " + connectionID;
        }

        public long getConnectionID() {
            return connectionID;
        }

        public void setConnectionID(long connectionID) {
            this.connectionID = connectionID;
        }
    }
}
