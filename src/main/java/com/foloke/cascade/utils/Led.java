package com.foloke.cascade.utils;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Led {
    private final double h;
    private final double w;
    private final double x;
    private final double y;
    private Color color;

    private int ledCounter = 0;

    public Led (Color color, double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
        this.color = color;
    }

    public void activate(double seconds) {
        ledCounter = (int) (seconds * 60);
    }

    public void activate() {
        ledCounter = -1;
    }

    public void render(GraphicsContext graphicsContext) {
        if (ledCounter == 0)
            return;

        if (ledCounter > 0) {
            ledCounter--;
        }

        graphicsContext.setFill(color);
        //graphicsContext.fillRect(parent.getX() + x, parent.getY() + y, w, h);
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
