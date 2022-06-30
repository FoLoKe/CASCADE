package com.foloke.cascade.Entities;

import com.badlogic.ashley.core.Entity;
import com.foloke.cascade.Components.CameraComponent;
import com.foloke.cascade.Components.MouseInputComponent;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.Tags.MainCameraTag;
import com.foloke.cascade.Components.VelocityComponent;
import javafx.scene.canvas.GraphicsContext;

public class Camera {

    private Camera() {}

    public static Entity instance(GraphicsContext graphicsContext, MouseInputComponent mouseInputComponent) {
        Entity entity = new Entity();

        entity.add(new CameraComponent(graphicsContext, 4));
        entity.add(new MainCameraTag());
        entity.add(new VelocityComponent());
        entity.add(new PositionComponent());
        entity.add(mouseInputComponent);

        return entity;
    }
}
