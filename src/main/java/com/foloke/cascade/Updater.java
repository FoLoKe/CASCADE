package com.foloke.cascade;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.foloke.cascade.Components.*;
import com.foloke.cascade.Components.Network.AddressComponent;
import com.foloke.cascade.Components.Tags.UIControllerTag;
import com.foloke.cascade.Entities.Camera;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Port;
import com.foloke.cascade.Systems.*;
import com.foloke.cascade.utils.EcsHelper;
import com.foloke.cascade.utils.QuadCollision;
import com.foloke.cascade.utils.ScanUtils;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;

public class Updater extends AnimationTimer {
    private final Engine engine;

    private final Object runLock = new Object();
    private final List<Runnable> tasks = new ArrayList<>();

    private final MouseInputComponent mouseInputComponent = new MouseInputComponent();
    private final GraphicsContext gc;

    private long previousTimestamp;

    public Updater(Canvas canvas) {
        //GRAPHICS
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.color(0.5D, 0.6D, 0.5D));
        gc.setImageSmoothing(false);

        //ECS
        engine = new Engine();

        // input
        engine.addSystem(new MouseInputSystem());

        // ui
        engine.addSystem(new ContextMenuSystem());

        // positioning
        engine.addSystem(new MovementSystem());
        engine.addSystem(new CollisionSystem());

        // network
        engine.addSystem(new PingSystem());

        // rendering
        engine.addSystem(new CameraSystem());
        engine.addSystem(new SpriteRenderSystem(gc));
        engine.addSystem(new SelectionRendererSystem(gc));
        engine.addSystem(new CollisionDebugRendererSystem(gc));

        Entity camera = new Camera(gc, mouseInputComponent);
        engine.addEntity(camera);

        Entity local = Device.instance(50, 50);
        engine.addEntity(local);
        engine.addEntity(Device.instance(25, 25));
        engine.addEntity(Device.instance(50, 25));

        Entity uiDummy = new Entity();
        uiDummy.add(new UIControllerTag());
        uiDummy.add(new ContextMenuComponent(canvas));
        engine.addEntity(uiDummy);

        ChildrenComponent localChildren = EcsHelper.ccm.get(local);
        PositionComponent positionComponent = EcsHelper.posCm.get(local);
        CollisionComponent localCollision = EcsHelper.colCm.get(local);
        QuadCollision localQuad = localCollision.hitBox;

        List<NetworkInterface> localInterfaces = ScanUtils.getLocalPorts();
        double offsetX = positionComponent.x + localQuad.getWidth() / 2f - localInterfaces.size() * Port.baseSize / 2f;
        double offsetY = positionComponent.y + localQuad.getHeight();

        for (int i = 0; i < localInterfaces.size(); i++) {
            NetworkInterface networkInterface = localInterfaces.get(i);
            List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();

            Entity port = Port.instance(offsetX + i * Port.baseSize, offsetY);
            port.add(new AddressComponent(addresses));
            engine.addEntity(port);

            localChildren.children.add(port);
        }
    }

    public void handle(long timestamp) {
        Canvas canvas = gc.getCanvas();
        gc.fillRect(0.0D, 0.0D, canvas.getWidth(), canvas.getHeight());
        gc.save();
        synchronized (runLock) {
            for (Runnable task : tasks) {
                task.run();
            }

            tasks.clear();
        }

        engine.update((timestamp - previousTimestamp) / 1000000f);
        previousTimestamp = timestamp;
        gc.restore();
    }

    public void runOnECS(Runnable runnable) {
        synchronized (runLock) {
            tasks.add(runnable);
        }
    }

    public void mouseInput(MouseEvent mouseEvent) {
        runOnECS(() -> mouseInputComponent.events.add(mouseEvent));
    }

    public void mouseScroll(ScrollEvent scrollEvent) {
        runOnECS(() -> mouseInputComponent.scroll = scrollEvent.getDeltaY() > 0 ? 0.25 : -0.25);
    }

    public void spawnEntityLater(Entity entity) {
        runOnECS(() -> engine.addEntity(entity));
    }

    public void addComponentLater(Entity entity, Component component) {
        runOnECS(() -> entity.add(component));
    }

    public void assignChildLater(Entity entity, Entity child) {
        runOnECS(() -> {
            if (!EcsHelper.ccm.has(entity)) {
                entity.add(new ChildrenComponent());
            }

            ChildrenComponent cm = EcsHelper.ccm.get(entity);
            cm.children.add(child);

            if (EcsHelper.pcm.has(child)) {
                ParentComponent pc = EcsHelper.pcm.get(child);
                pc.parent = entity;
            } else {
                ParentComponent pc = new ParentComponent();
                pc.parent = entity;
                child.add(pc);
            }
        });
    }

    public void removeComponentLater(Entity entity, Class<? extends Component> componentClass) {
        runOnECS(() -> {
            entity.remove(componentClass);
        });
    }
}