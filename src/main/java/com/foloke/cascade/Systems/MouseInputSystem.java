package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.CameraComponent;
import com.foloke.cascade.Components.CollisionComponent;
import com.foloke.cascade.Components.MouseInputComponent;
import com.foloke.cascade.Components.Tags.MainCameraTag;
import com.foloke.cascade.Components.Tags.SelectedTag;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class MouseInputSystem extends EntitySystem {
    private ImmutableArray<Entity> consumers;
    private ImmutableArray<Entity> cameras;
    private ImmutableArray<Entity> entities;

    private final ComponentMapper<MouseInputComponent> mm = ComponentMapper.getFor(MouseInputComponent.class);
    private final ComponentMapper<CameraComponent> cm = ComponentMapper.getFor(CameraComponent.class);
    private final ComponentMapper<CollisionComponent> colm = ComponentMapper.getFor(CollisionComponent.class);
    private final ComponentMapper<SelectedTag> sm = ComponentMapper.getFor(SelectedTag.class);

    @Override
    public void addedToEngine(Engine engine) {
        consumers = engine.getEntitiesFor(Family.all(MouseInputComponent.class).get());
        cameras = engine.getEntitiesFor(Family.all(CameraComponent.class, MainCameraTag.class).get());
        entities = engine.getEntitiesFor(Family.all(CollisionComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity consumer : consumers) {
            MouseInputComponent inputComponent = mm.get(consumer);

            for (Entity camera : cameras) {
                CameraComponent cameraComponent = cm.get(camera);

                //TODO: Relative scale
                if (inputComponent.scroll != 0)
                    cameraComponent.scale = Math.max(2, Math.min(cameraComponent.scale + inputComponent.scroll, 8));

                //TODO: HIT ENTITIES
                for (MouseEvent mouseEvent : inputComponent.events) {
                    EventType<? extends MouseEvent> eventType = mouseEvent.getEventType();

                    System.out.println("event: " + eventType.getName());
                    if (eventType == MouseEvent.MOUSE_CLICKED) {
                        System.out.println("click");
                        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                            System.out.println("primary");
                            Point2D hitPoint = screenToWorld(cameraComponent, mouseEvent.getX(), mouseEvent.getY());
                            for (Entity entity : entities) {
                                CollisionComponent collisionComponent = colm.get(entity);
                                //TODO: PARENTING
                                if (collisionComponent.hitBox.contains(hitPoint)) {
                                    if (sm.has(entity)) {
                                        entity.remove(SelectedTag.class);
                                    } else {
                                        entity.add(new SelectedTag());
                                    }
                                }
                            }
                        }
                    } else if (eventType == MouseEvent.MOUSE_DRAGGED) {
                        //TODO: DRAG
                    }
                }

                inputComponent.events.clear();

                inputComponent.scroll = 0;
            }
        }
    }

    public static Point2D screenToWorld(CameraComponent cameraComponent, double x, double y) {
        return new Point2D(x / cameraComponent.scale - cameraComponent.x, y / cameraComponent.scale - cameraComponent.y);
    }
}
