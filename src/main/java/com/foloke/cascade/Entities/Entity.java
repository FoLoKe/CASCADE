package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.Timer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;


public abstract class Entity {
    public long ID;
    static long counter;
    public boolean destroyed;
    public boolean selected;

    public Group group;

    protected String name = "name";

    protected  ArrayList<Timer> timers;

    protected Rectangle rectangle;
    public MapController mapController;

    public Entity(MapController mapController) {
        ID = counter;
        name += counter++;

        init(mapController);
    }

    public Entity(MapController mapController, String[] params) {
        ID = Integer.parseInt(params[1]);
        name = params[2];
        init(mapController);
    }

    private void init(MapController mapController) {
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

    public void move(Point2D point2D) {
        setLocation(rectangle.getX() + point2D.getX(),rectangle.getY() + point2D.getY());
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

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getID() {
        return ID;
    }

    public void cleanup(){

    }

    public String getSave() {
        return ID + " " + name
                + " " + rectangle.getX()
                + " " + rectangle.getY();
    }
}