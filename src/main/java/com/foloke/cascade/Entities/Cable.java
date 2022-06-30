package com.foloke.cascade.Entities;

import com.badlogic.ashley.core.Entity;
import com.foloke.cascade.Application;
import com.foloke.cascade.Components.*;
import com.foloke.cascade.utils.Sprite;

public class Cable {

    private Cable() {}

    public static Entity instance() {
        Entity entity = new Entity();
        // TODO: Component with two links, and ability to change cable formats

        return entity;
    }

    public static class Connector {

        private Connector() {}

        public static Entity Instance() {
            Entity entity = new Entity();
            entity.add(new PositionComponent());
            entity.add(new VelocityComponent());
            entity.add(new CollisionComponent(8, 8));
            entity.add(new SpriteComponent(Sprite.create(Application.spriteSheet, 0, 0, 8, 8)));

            return entity;
        }
    }
}
