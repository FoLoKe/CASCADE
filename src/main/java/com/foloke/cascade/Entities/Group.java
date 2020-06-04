package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;

public class Group extends Entity {
    ArrayList<Entity> entities;

    public Group(MapController mapController) {
        super(mapController);
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
        gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        gc.fillText(name, rectangle.getX(), rectangle.getY() + 5);
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

        if (rectangle.contains(point2D)) {
            return this;
        }

        return null;
    }

    @Override
    public void setLocation(double x, double y) {
        Point2D offset = new Point2D(x - rectangle.getX(), y - rectangle.getY());
        for(Entity entity : entities) {
            entity.move(offset);
        }
        super.setLocation(x, y);
    }

    public void addToGroup(Entity entity) {
        if(!entities.contains(entity)) {
            entities.add(entity);
            entity.group = this;
        }
    }

    public void removeFromGroup(Entity entity) {
        if(entities.contains(entity)) {
            entities.remove(entity);
            entity.group = null;

            System.out.println(entity);
        }
    }
}