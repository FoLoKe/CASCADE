package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.CameraComponent;
import com.foloke.cascade.Components.Tags.MainCameraTag;
import javafx.scene.canvas.GraphicsContext;

public class CameraSystem extends EntitySystem {
    private ImmutableArray<Entity> cameras;

    private final ComponentMapper<CameraComponent> cm = ComponentMapper.getFor(CameraComponent.class);

    private final GraphicsContext gc;

    public CameraSystem(GraphicsContext graphicsContext) {
        this.gc = graphicsContext;
    }

    @Override
    public void addedToEngine(Engine engine) {
        cameras = engine.getEntitiesFor(Family.all(CameraComponent.class, MainCameraTag.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity camera : cameras) {
            CameraComponent cc = cm.get(camera);
            gc.scale(cc.scale, cc.scale);
            gc.translate(cc.x, cc.y);
            return; // only first active camera
        }
    }
}
