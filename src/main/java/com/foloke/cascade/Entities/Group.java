package com.foloke.cascade.Entities;

import com.foloke.cascade.Controllers.MapController;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class Group extends Entity {
    ArrayList<Device> devices;

    public Group(MapController mapController) {
        super(mapController);
    }

    @Override
    public void render(GraphicsContext gc) {

    }

    @Override
    public Entity hit(Point2D point2D) {
        Entity selected;
        for (Entity entity : devices) {
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

    public void addToGroup(Device device) {
        if(!devices.contains(device)) {
            devices.add(device);
        }
    }
}
