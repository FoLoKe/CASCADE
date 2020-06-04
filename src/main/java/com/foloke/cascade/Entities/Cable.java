package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.LogUtils;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Cable extends Entity {

    public final Connector connectorA;
    public final Connector connectorB;

    public Cable(MapController mapController) {
        super(mapController);
        connectorA = new Connector(this);
        connectorB = new Connector(this);
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

    public static class Connector extends Entity {
        public Device.Port connection;
        private final Cable parent;

        public Connector(Cable parent) {
            super(parent.mapController);
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

        public void connect(Device.Port connection) {
            this.connection = connection;
            connection.connect(this);
            LogUtils.log(connection.address + " connected with cable " + this);
        }

        public void disconnect() {
            if(connection != null) {
                connection.disconnect(this);
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
            parent.destroy();
        }
    }
}
