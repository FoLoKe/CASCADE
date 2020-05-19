package com.foloke.cascade;

import javafx.geometry.Point2D;

public class Camera {
    public double x;
    public double y;
    public float scale;

    public Camera(float x, float y, float scale) {
        this.scale = scale;
        this.x = x;
        this.y = y;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(Point2D point2D) {
        this.x = point2D.getX();
        this.y = point2D.getY();
    }

    public Point2D translate(double tx, double ty) {
        return new Point2D(tx / scale - x, ty / scale - y);
    }

    public void zoomIn() {
        scale = Float.min(10, scale + 0.25f);
    }

    public void zoomOut() {
        scale = Float.max(2f, scale -= 0.25f);
    }
}
