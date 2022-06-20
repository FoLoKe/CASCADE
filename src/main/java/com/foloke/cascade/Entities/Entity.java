package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.Timer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {
    public long ID;
    static long counter;
    public boolean destroyed;
    public boolean selected;

    public Group group;

    protected String name = "name";

    protected  ArrayList<Timer> timers = new ArrayList<>();;

    public Rectangle hitBox = new Rectangle(16, 16);
    public MapController mapController;

    public Entity parent;
    public List<Entity> children = new ArrayList<>();

    public Entity(MapController mapController) {
        ID = counter;
        name += counter++;
        this.mapController = mapController;
    }

    public abstract void render(GraphicsContext var1);

    public void tick(long timestamp) {
        for(Timer timer : timers) {
            timer.tick(timestamp);
        }
    }

    private Point2D local = Point2D.ZERO;

    public void setPosition(double x, double y) {
        if (parent != null) {
            hitBox.setX(x + local.getX());
            hitBox.setY(y + local.getY());
        } else {
            hitBox.setX(x);
            hitBox.setY(y);
        }

        for (Entity child : children) {
            System.out.println("child turn");
            child.setPosition(x, y);
        }
    }

    public void setLocalPosition(double x, double y) {
        if(parent != null) {
            local = new Point2D(x, y);
        }

        setPosition(getX(), getY());
    }

    public void moveBy(Point2D point2D) {
        setPosition(hitBox.getX() + point2D.getX(), hitBox.getY() + point2D.getY());
    }

    public double getX() {
        return hitBox.getX();
    }

    public double getY() {
        return hitBox.getY();
    }

    public Entity hit(Point2D hitPoint) {
        if (hitBox.contains(hitPoint)) {
            return this;
        }
        for (Entity child : children) {
            Entity entity = child.hit(hitPoint);
            if(entity != null) {
                return entity;
            }
        }
        return null;
    }

    public void addTask(Timer timer) {
        timers.add(timer);
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void destroy() {
        destroyed = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(this instanceof Device) {
            LogUtils.logToFile(name, "name has changed: " + this.name + " to " + name);
        }
        this.name = name;
    }

    public long getID() {
        return ID;
    }

    public void cleanup() {

    }

    public void add(Entity child) {
        children.add(child);
        child.parent = this;
        child.local.add(getX() - child.getX(), getY() - child.getY());
    }
}