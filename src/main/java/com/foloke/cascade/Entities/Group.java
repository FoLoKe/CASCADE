package com.foloke.cascade.Entities;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class Group extends Entity {
    ArrayList<Entity> entities;
    public String ids;

    public Group() {
        super();
        init();
    }

    private void init() {
        entities = new ArrayList<>();
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1d);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("sans", 5));
        gc.strokeRect(hitBox.getX(), hitBox.getY(), hitBox.getWidth(), hitBox.getHeight());
        gc.fillText(name, hitBox.getX(), hitBox.getY() + 5);
    }

    @Override
    public Entity hit(Point2D point2D) {
        Entity selected;
        for (Entity entity : entities) {
            selected = entity.hit(point2D);
            if(selected != null) {
                return selected;
            }
        }

        if (hitBox.contains(point2D)) {
            return this;
        }

        return null;
    }

    @Override
    public void setPosition(double x, double y) {
        Point2D offset = new Point2D(x - hitBox.getX(), y - hitBox.getY());
        for(Entity entity : entities) {
            entity.moveBy(offset);
        }
        super.setPosition(x, y);
    }

    public void addToGroup(Entity entity) {
        if(entity.group == null) {
            if (!entities.contains(entity)) {
                entities.add(entity);
                entity.group = this;
            }
        }
    }

    public void removeFromGroup(Entity entity) {
        if(entities.contains(entity)) {
            entities.remove(entity);
            entity.group = null;

            System.out.println(entity);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void cleanup() {
        for (Entity entity : entities) {
            entity.group = null;
        }

        entities.clear();
    }
}
