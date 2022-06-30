package com.foloke.cascade.Entities;

import com.badlogic.ashley.core.Entity;
import com.foloke.cascade.Application;
import com.foloke.cascade.Components.*;
import com.foloke.cascade.Components.Network.SnmpComponent;
import com.foloke.cascade.Components.Tags.DeviceTag;
import com.foloke.cascade.utils.Sprite;

public class Device {

    private Device() {
    }

    //TODO: MAKE JSON OR DATABASE SAVE

    public static Entity instance(double x, double y) {
        Entity device = new Entity();
        device.add(new PositionComponent(x, y));
        device.add(new VelocityComponent());
        device.add(new SpriteComponent(Sprite.create(Application.spriteSheet, 0, 0, 16, 16, 1)));
        device.add(new CollisionComponent());
        device.add(new DeviceTag());
        device.add(new ChildrenComponent());
        device.add(new SnmpComponent());

        return device;
    }
}
