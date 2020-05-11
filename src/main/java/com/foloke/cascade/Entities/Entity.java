package com.foloke.cascade.Entities;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;


public abstract class Entity {
    protected float x;
    protected float y;
    public boolean destroyed;

    private Rectangle rectangle;

    public Entity() {
        this.rectangle = new Rectangle(16, 16);
    }

    public abstract void render(GraphicsContext var1);

    public abstract void tick();

    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
        updatePosition();
    }

    public void setLocation(Point2D point2D) {
        this.x = (float) point2D.getX();
        this.y = (float) point2D.getY();
        updatePosition();
    }

    public Rectangle getHitBox() {
        return rectangle;
    }

    protected void updatePosition() {
        rectangle.setX(x);
        rectangle.setY(y);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}