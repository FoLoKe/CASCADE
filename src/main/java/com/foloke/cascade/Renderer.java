package com.foloke.cascade;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Renderer extends AnimationTimer {
    private Canvas canvas;
    private Application application;

    public Renderer(Application application) {
        this.application = application;
        this.canvas = application.uiController.getCanvas();
    }

    public void handle(long l) {
        GraphicsContext graphicsContext = this.canvas.getGraphicsContext2D();
        graphicsContext.setImageSmoothing(false);
        graphicsContext.setFill(Color.color(0.5D, 0.6D, 0.5D));
        graphicsContext.fillRect(0.0D, 0.0D, this.canvas.getWidth(), this.canvas.getHeight());
        graphicsContext.save();
        this.application.mapController.render(graphicsContext);
        graphicsContext.restore();
        graphicsContext.setLineWidth(1.0D);
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillText("TEST", 10.0D, 10.0D);


    }
}