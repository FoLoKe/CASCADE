package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import javafx.scene.shape.Rectangle;

public class CollisionComponent implements Component {
    public Rectangle hitBox = new Rectangle(16, 16);
}
