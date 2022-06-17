package com.foloke.cascade.Entities;

import com.foloke.cascade.Application;
import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.Sprite;
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

    @Override
    public void render(GraphicsContext gc) {
        connectorA.render(gc);
        connectorB.render(gc);

        gc.setLineWidth(1.5f);
        gc.setStroke(Color.BLACK);
        gc.strokeLine(connectorA.getX() + connectorA.getHitBox().getWidth() / 2f,
                connectorA.getY() + connectorA.getHitBox().getHeight() / 2f + 1f,
                connectorB.getX() + connectorB.getHitBox().getWidth() / 2f,
                connectorB.getY() + connectorB.getHitBox().getHeight() / 2f + 1f);
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
        public Port connection;
        private long connectionID;
        private Cable parent;
        private Sprite sprite;

        public Connector(Cable parent) {
            super(parent.mapController);
            this.sprite = Sprite.create(Application.spriteSheet, 24, 0, 8, 8, 1);
            init(parent);
        }

        private void init(Cable parent) {
            this.parent = parent;
            hitBox = new Rectangle(8, 8);
        }

        @Override
        public void render(GraphicsContext gc) {
            sprite.render(gc, getX(), getY());
        }

        @Override
        public void tick(long timestamp) {
            if(connection != null) {
                hitBox.setX(connection.getX());
                hitBox.setY(connection.getY());
            }
        }

        public void connect(Port connection) {
            this.connection = connection;
            connectionID = connection.getID();
            connection.connect(this);
            LogUtils.log(connection.primaryAddress + " connected with cable " + this);
        }

        public void disconnect() {
            if(connection != null) {
                connection.disconnect(this);
                connectionID = -1;
                LogUtils.log(connection.primaryAddress + " disconnected with cable " + this);
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

        public long getConnectionID() {
            return connectionID;
        }

        public void setConnectionID(long connectionID) {
            this.connectionID = connectionID;
        }
    }
}
