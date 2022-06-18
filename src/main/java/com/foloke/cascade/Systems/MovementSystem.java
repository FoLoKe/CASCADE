package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.*;

public class MovementSystem extends EntitySystem {
    ImmutableArray<Entity> entities;

    ComponentMapper<PositionComponent> pcm = ComponentMapper.getFor(PositionComponent.class);
    ComponentMapper<VelocityComponent> vcm = ComponentMapper.getFor(VelocityComponent.class);
    ComponentMapper<CollisionComponent> ccm = ComponentMapper.getFor(CollisionComponent.class);

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PositionComponent.class, VelocityComponent.class).exclude(ParentComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        entities.forEach(entity -> {
            VelocityComponent vc = vcm.get(entity);
            if(vc.dx == 0 && vc.dy == 0)
                return;

            PositionComponent pc = pcm.get(entity);
            pc.consumed = false;
            pc.x += vc.dx;
            pc.y += vc.dy;

            vc.dx = 0;
            vc.dy = 0;
        });
    }
}
