package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import com.foloke.cascade.utils.QuadCollision;

public class CollisionComponent implements Component {
    public QuadCollision hitBox = new QuadCollision(16, 16);
}
