package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.CameraComponent;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.Tags.MainCameraTag;

public class CameraSystem extends EntitySystem {
    private ImmutableArray<Entity> cameras;

    private final ComponentMapper<CameraComponent> cm = ComponentMapper.getFor(CameraComponent.class);
    private final ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);

    @Override
    public void addedToEngine(Engine engine) {
        cameras = engine.getEntitiesFor(Family.all(PositionComponent.class, CameraComponent.class, MainCameraTag.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity camera : cameras) {
            CameraComponent cc = cm.get(camera);
            PositionComponent pc = pm.get(camera);

            cc.gc.scale(cc.scale, cc.scale);
            cc.gc.translate(pc.x, pc.y);
            pc.consumed = true;
            return; // only first active camera
        }
    }
}
