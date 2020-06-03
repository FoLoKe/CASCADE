package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;


public abstract class Entity {
    public boolean destroyed;
    public boolean selected;

    protected Rectangle rectangle;
    MapController mapController;

    public Entity(MapController mapController) {
        this.mapController = mapController;
        this.rectangle = new Rectangle(16, 16);
    }

    public abstract void render(GraphicsContext var1);

    public abstract void tick(long timestamp);

    public void setLocation(double x, double y) {
        rectangle.setX(x);
        rectangle.setY(y);
    }

    public double getX() {
        return rectangle.getX();
    }

    public double getY() {
        return rectangle.getY();
    }

    public abstract Entity hit(Point2D point2D);

    public Rectangle getHitBox() {
        return rectangle;
    }
}