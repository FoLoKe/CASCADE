package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.Network.AddressComponent;
import com.foloke.cascade.Components.Network.PingComponent;
import com.foloke.cascade.utils.ScanUtils;

public class PingSystem extends EntitySystem {
    ImmutableArray<Entity> pinging;

    ComponentMapper<PingComponent> pcm = ComponentMapper.getFor(PingComponent.class);
    ComponentMapper<AddressComponent> acm = ComponentMapper.getFor(AddressComponent.class);

    @Override
    public void addedToEngine(Engine engine) {
        pinging = engine.getEntitiesFor(Family.all(PingComponent.class, AddressComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        pinging.forEach(entity -> {
            PingComponent pc = pcm.get(entity);
            if (pc.pinging.get())
                return;

            pc.timestamp += deltaTime;
            if (pc.timestamp > pc.delay) {
                System.out.println(pc.timestamp > pc.delay);
                AddressComponent ac = acm.get(entity);
                ScanUtils.ping(pc, ac);
                pc.timestamp = 0;
            }
        });
    }
}
