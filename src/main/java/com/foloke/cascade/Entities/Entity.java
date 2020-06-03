package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.Timer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;


public abstract class Entity {
    public boolean destroyed;
    public boolean selected;

    protected final ArrayList<Timer> timers;

    protected Rectangle rectangle;
    public MapController mapController;

    public Entity(MapController mapController) {
        this.timers = new ArrayList<>();
        this.mapController = mapController;
        this.rectangle = new Rectangle(16, 16);
    }

    public abstract void render(GraphicsContext var1);

    public void tick(long timestamp) {
        for(Timer timer : timers) {
            timer.tick(timestamp);
        }
    }

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

    public void addTask(Timer timer) {
        timers.add(timer);
    }

    public Rectangle getHitBox() {
        return rectangle;
    }
}