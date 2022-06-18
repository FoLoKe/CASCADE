package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import javafx.scene.canvas.GraphicsContext;

public class CameraComponent implements Component {
    public GraphicsContext gc;
    public double scale;

    public CameraComponent(GraphicsContext graphicsContext, float scale) {
        this.gc = graphicsContext;
        this.scale = scale;
    }
}
