package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

public class Cable extends Entity {

    private final Connector connectorA;
    private final Connector connectorB;

    public Cable(MapController mapController) {
        super(mapController);
        connectorA = new Connector(this);
        connectorB = new Connector(this);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.strokeLine(connectorA.getX() + connectorA.rectangle.getWidth() / 2,
                connectorA.getY() + connectorA.rectangle.getHeight() / 2,
                connectorB.getX() + connectorB.rectangle.getWidth() / 2,
                connectorB.getY() + connectorB.rectangle.getHeight() / 2);
        connectorA.render(gc);
        connectorB.render(gc);
    }

    @Override
    public void tick() {
        connectorA.tick();
        connectorB.tick();
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

    public static class Connector extends Entity {
        private Device.Port connection;

        public Connector(Cable parent) {
            super(parent.mapController);
            rectangle = new Rectangle(5, 5);
        }

        @Override
        public void render(GraphicsContext gc) {
            gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        }

        @Override
        public void tick() {
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
            connection.connector = this;
        }

        public void disconnect() {
            if(connection != null) {
                connection.connector = null;
                this.connection = null;
            }
        }

        @Override
        public void drop(double x, double y) {

        }
    }
}
