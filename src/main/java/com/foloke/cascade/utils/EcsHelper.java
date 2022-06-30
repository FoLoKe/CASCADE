package com.foloke.cascade.utils;

import com.badlogic.ashley.core.ComponentMapper;
import com.foloke.cascade.Components.*;
import com.foloke.cascade.Components.Network.AddressComponent;
import com.foloke.cascade.Components.Network.PingComponent;
import com.foloke.cascade.Components.Network.SnmpComponent;
import com.foloke.cascade.Components.Tags.DeviceTag;
import com.foloke.cascade.Components.Tags.PortTag;

public class EcsHelper {
    // positioning
    public static final ComponentMapper<ChildrenComponent> ccm = ComponentMapper.getFor(ChildrenComponent.class);
    public static final ComponentMapper<CollisionComponent> colCm = ComponentMapper.getFor(CollisionComponent.class);
    public static final ComponentMapper<ParentComponent> pcm = ComponentMapper.getFor(ParentComponent.class);
    public static final ComponentMapper<PositionComponent> posCm = ComponentMapper.getFor(PositionComponent.class);

    // ui
    public static final ComponentMapper<ContextMenuComponent> cmCm = ComponentMapper.getFor(ContextMenuComponent.class);

    // tags
    public static final ComponentMapper<DeviceTag> dtCm = ComponentMapper.getFor(DeviceTag.class);
    public static final ComponentMapper<PortTag> ptCm = ComponentMapper.getFor(PortTag.class);

    // network
    public static final ComponentMapper<PingComponent> pingCm = ComponentMapper.getFor(PingComponent.class);
    public static final ComponentMapper<AddressComponent> aCm = ComponentMapper.getFor(AddressComponent.class);
    public static final ComponentMapper<SnmpComponent> snmpCm = ComponentMapper.getFor(SnmpComponent.class);
}
