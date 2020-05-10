package com.foloke.cascade.Controllers;

import com.foloke.cascade.Camera;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class MapController {
    private List<Entity> entityList = new ArrayList<>();
    private Camera camera;
    private Device selected;

    public MapController() {
        Image image = new Image("/images/spritesheet.png", 16.0D, 16.0D, false, false);
        this.entityList.add(new Device(image));
        this.camera = new Camera();
        this.camera.scale = 4.0F;
    }

    public void render(GraphicsContext gc) {
        gc.scale((double)this.camera.scale, (double)this.camera.scale);
        Iterator var2 = this.entityList.iterator();

        while(var2.hasNext()) {
            Entity entity = (Entity)var2.next();
            entity.render(gc);
        }

        if (this.selected != null) {
            Rectangle rectangle = this.selected.getHitBox();
            gc.setLineWidth(5.0D);
            gc.setStroke(Color.YELLOW);
            gc.strokeRect((double)rectangle.x, (double)rectangle.y, (double)rectangle.width, (double)rectangle.height);
        }

    }

    public void tick() {
        Iterator var1 = this.entityList.iterator();

        while(var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            entity.tick();
        }

    }

    public void addEntity(Entity entity) {
        this.entityList.add(entity);
    }

    public void pick(double x, double y) {
        Iterator var5 = this.entityList.iterator();

        Entity entity;
        do {
            if (!var5.hasNext()) {
                this.selected = null;
                return;
            }

            entity = (Entity)var5.next();
        } while(!(entity instanceof Device) || !((Device)entity).getHitBox().contains(x, y));

        this.selected = (Device)entity;
    }
}