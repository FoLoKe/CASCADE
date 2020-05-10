package com.foloke.cascade.Entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.awt.Rectangle;

public class Device extends Entity {
    Image image;
    Rectangle rectangle;
    public Device(Image image) {
        this.image = image;
        this.rectangle = new Rectangle(16, 16);
    }

    @Override
    public void render(GraphicsContext context) {
        context.drawImage(image, x, y);
    }

    @Override
    public void tick() {

    }

    public Rectangle getHitBox() {
        return rectangle;
    }
}
