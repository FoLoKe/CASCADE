package com.foloke.cascade.Entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Device extends Entity {
    Image image;

    public Device(Image image) {
        this.image = image;
    }

    @Override
    public void render(GraphicsContext context) {
        context.drawImage(image, x, y);
    }

    @Override
    public void tick() {

    }


}
