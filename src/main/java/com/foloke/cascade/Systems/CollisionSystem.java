package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.CollisionComponent;
import com.foloke.cascade.Components.PositionComponent;

public class CollisionSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    private final ComponentMapper<CollisionComponent> ccm = ComponentMapper.getFor(CollisionComponent.class);
    private final ComponentMapper<PositionComponent> pcm = ComponentMapper.getFor(PositionComponent.class);

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PositionComponent.class, CollisionComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        entities.forEach(entity -> {
            PositionComponent pc = pcm.get(entity);
            if (!pc.consumed) {
                CollisionComponent cc = ccm.get(entity);
                cc.hitBox.setPosition(pc.x, pc.y);
                pc.consumed = true;

                // TODO: calculate grouping AABB vs AABB with GROUP tag
            }
        });
    }
}
