package com.foloke.cascade.Entities;

import com.badlogic.ashley.core.Entity;
import com.foloke.cascade.Components.CameraComponent;
import com.foloke.cascade.Components.MouseInputComponent;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.Tags.MainCameraTag;
import com.foloke.cascade.Components.VelocityComponent;
import javafx.scene.canvas.GraphicsContext;

public class Camera extends Entity {

    public Camera (GraphicsContext graphicsContext, MouseInputComponent mouseInputComponent) {
        add(new CameraComponent(graphicsContext, 4));
        add(new MainCameraTag());
        add(new VelocityComponent());
        add(new PositionComponent());
        add(mouseInputComponent);
    }
}
