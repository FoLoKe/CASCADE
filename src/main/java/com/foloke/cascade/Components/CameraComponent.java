package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;

public class CameraComponent implements Component {
    public double x;
    public double y;
    public double scale;

    public CameraComponent(float x, float y, float scale) {
        this.scale = scale;
        this.x = x;
        this.y = y;
    }
}
