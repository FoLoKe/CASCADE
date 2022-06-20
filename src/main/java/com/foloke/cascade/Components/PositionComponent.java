package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;

public class PositionComponent implements Component {
    public double x;
    public double y;
    public boolean consumed;

    public PositionComponent() {}

    public PositionComponent(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
