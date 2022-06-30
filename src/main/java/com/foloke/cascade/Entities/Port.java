package com.foloke.cascade.Entities;

import com.badlogic.ashley.core.Entity;
import com.foloke.cascade.Application;
import com.foloke.cascade.Components.CollisionComponent;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.SpriteComponent;
import com.foloke.cascade.Components.Tags.PortTag;
import com.foloke.cascade.Components.VelocityComponent;
import com.foloke.cascade.utils.*;

public class Port {

    private  Port() {}

    public static Entity instance(double x, double y) {
        Entity port = new Entity();
        port.add(new PositionComponent(x, y));
        port.add(new VelocityComponent());
        port.add(new SpriteComponent(Sprite.create(Application.spriteSheet, 16, 0, 8, 8, 1)));
        port.add(new CollisionComponent(8, 8));
        port.add(new PortTag());
        return port;
    }
}

