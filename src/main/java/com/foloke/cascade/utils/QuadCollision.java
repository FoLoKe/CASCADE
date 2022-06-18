package com.foloke.cascade.utils;

import javafx.geometry.Point2D;

public class QuadCollision {
    public Vector2 min;
    public Vector2 max;

    public QuadCollision(double width, double height) {
        this.min = new Vector2();
        this.max = new Vector2(width, height);
    }

    public boolean overlaps(QuadCollision quadCollision) {
        if(max.x < quadCollision.min.x || min.x > quadCollision.max.x)
            return false;

        return !(max.y < quadCollision.min.y) && !(min.y > quadCollision.max.y);
    }

    public void setPosition(double x, double y) {
        max.x = x + max.x - min.x;
        max.y = y + max.y - min.y;
        min.x = x;
        min.y = y;
    }

    public boolean contains(Point2D hitPoint) {
        return hitPoint.getX() >= min.x
                && hitPoint.getY() >= min.y
                && hitPoint.getX() <= max.x
                && hitPoint.getY() <= max.y;
    }

    public double getX() {
        return min.x;
    }

    public double getY() {
        return min.y;
    }

    public double getWidth() {
        return max.x - min.x;
    }

    public double getHeight() {
        return max.y - min.y;
    }

    private static class Vector2 {
        public double x;
        public double y;

        public Vector2() {}

        public Vector2(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void add(double x, double y) {
            this.x += x;
            this.y += y;
        }
    }
}
