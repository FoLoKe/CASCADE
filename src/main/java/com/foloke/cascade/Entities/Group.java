package com.foloke.cascade.Entities;

import com.badlogic.ashley.core.Entity;
import com.foloke.cascade.Components.ChildrenComponent;
import com.foloke.cascade.Components.CollisionComponent;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.VelocityComponent;

public class Group {

    private Group() {}

    public static Entity instance() {
        Entity entity = new Entity();
        entity.add(new PositionComponent());
        entity.add(new CollisionComponent());
        entity.add(new VelocityComponent());
        entity.add(new ChildrenComponent());

        return entity;
    }
}
