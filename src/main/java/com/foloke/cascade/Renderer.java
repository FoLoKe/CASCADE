package com.foloke.cascade;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Renderer extends AnimationTimer {
    private final Canvas canvas;
    private final Application application;

    public Renderer(Application application) {
        this.application = application;
        this.canvas = application.uiController.getCanvas();
    }

    public void handle(long timestamp) {
        GraphicsContext graphicsContext = this.canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.color(0.5D, 0.6D, 0.5D));
        graphicsContext.fillRect(0.0D, 0.0D, this.canvas.getWidth(), this.canvas.getHeight());
        graphicsContext.setImageSmoothing(false);
        graphicsContext.save();
        this.application.mapController.tick(timestamp);
        this.application.mapController.render(graphicsContext);
        graphicsContext.restore();
    }
}