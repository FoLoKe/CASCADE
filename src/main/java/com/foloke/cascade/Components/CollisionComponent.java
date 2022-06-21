package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import com.foloke.cascade.utils.QuadCollision;

public class CollisionComponent implements Component {
    public QuadCollision hitBox;

    public CollisionComponent() {
        hitBox = new QuadCollision(16, 16);
    }

    public CollisionComponent(double width, double height) {
        hitBox = new QuadCollision(width, height);
    }
}
