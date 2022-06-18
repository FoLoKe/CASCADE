package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.*;
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
    private ImmutableArray<Entity> selected;

    private final ComponentMapper<MouseInputComponent> mcm = ComponentMapper.getFor(MouseInputComponent.class);
    private final ComponentMapper<CameraComponent> ccm = ComponentMapper.getFor(CameraComponent.class);
    private final ComponentMapper<CollisionComponent> colCm = ComponentMapper.getFor(CollisionComponent.class);
    private final ComponentMapper<SelectedTag> scm = ComponentMapper.getFor(SelectedTag.class);
    private final ComponentMapper<VelocityComponent> vcm = ComponentMapper.getFor(VelocityComponent.class);
    private final ComponentMapper<PositionComponent> pcm = ComponentMapper.getFor(PositionComponent.class);

    private boolean dragInProgress;
    private boolean clickInProgress;

    @Override
    public void addedToEngine(Engine engine) {
        consumers = engine.getEntitiesFor(Family.all(MouseInputComponent.class).get());
        cameras = engine.getEntitiesFor(Family.all(PositionComponent.class, VelocityComponent.class, CameraComponent.class, MainCameraTag.class).get());
        entities = engine.getEntitiesFor(Family.all(VelocityComponent.class, CollisionComponent.class).get());
        selected = engine.getEntitiesFor(Family.all(SelectedTag.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity consumer : consumers) {
            MouseInputComponent inputComponent = mcm.get(consumer);

            for (Entity camera : cameras) {
                CameraComponent cameraComponent = ccm.get(camera);
                VelocityComponent camVc = vcm.get(camera);
                PositionComponent camPc = pcm.get(camera);

                //TODO: Relative scale
                if (inputComponent.scroll != 0)
                    cameraComponent.scale = Math.max(2, Math.min(cameraComponent.scale + inputComponent.scroll, 8));

                for (MouseEvent mouseEvent : inputComponent.events) {
                    onMouseEvent(mouseEvent, inputComponent, cameraComponent, camVc, camPc);
                }

                inputComponent.events.clear();

                inputComponent.scroll = 0;
                break; // only first main camera
            }
        }
    }

    private void onMouseEvent(MouseEvent mouseEvent, MouseInputComponent ic, CameraComponent cc, VelocityComponent camVc, PositionComponent camPc) {
        EventType<? extends MouseEvent> eventType = mouseEvent.getEventType();
        Point2D worldPoint = screenToWorld(cc, camPc, mouseEvent.getX(), mouseEvent.getY());


        if (eventType == MouseEvent.MOUSE_PRESSED) {
            clickInProgress = true;
        } else if (eventType == MouseEvent.MOUSE_RELEASED && clickInProgress) {
            System.out.println("click");
            Entity picked = pickEntity(worldPoint);
            if (!mouseEvent.isShiftDown()) {
                selected.forEach((entity -> entity.remove(SelectedTag.class)));
            }

            if(picked != null) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (scm.has(picked)) {
                        picked.remove(SelectedTag.class);
                    } else {
                        picked.add(new SelectedTag());
                    }
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY){
                    if (!scm.has(picked)) {
                        picked.add(new SelectedTag());
                    }
                    //TODO: toggle UI Entity's context menu
                }
            } else {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    //TODO: toggle UI context context menu
                }
            }

        } else if (eventType == MouseEvent.MOUSE_DRAGGED) {
            //TODO: DRAG
            clickInProgress = false;
            if (!dragInProgress) {
                ic.lastEvent = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                dragInProgress = true;
                if (selected.size() < 2) {
                    Entity picked = pickEntity(worldPoint);
                    if (picked == null) {
                        selected.forEach((entity -> entity.remove(SelectedTag.class)));
                    } else {
                        if (!scm.has(picked)) {
                            picked.add(new SelectedTag());
                        }
                    }
                }
            }

            double dx = mouseEvent.getX() - ic.lastEvent.getX();
            double dy = mouseEvent.getY() - ic.lastEvent.getY();
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                if (selected.size() == 0) {
                    if (mouseEvent.isShiftDown()) {
                        //TODO: group creation
                    } else {
                        //TODO: group selection
                    }
                } else {
                    selected.forEach(entity -> {
                        //if (entity.)
                    });
                }
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                Entity picked = pickEntity(worldPoint);
                if (picked != null && scm.has(picked)) {
                    // TODO: move entities
                } else {
                    camVc.dx += dx / cc.scale;
                    camVc.dy += dy / cc.scale;
                }
            }
        }

        ic.lastEvent = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    private Entity pickEntity(Point2D hitPoint) {
        for (Entity entity : entities) {
            CollisionComponent collisionComponent = colCm.get(entity);
            //TODO: PARENTING
            if (collisionComponent.hitBox.contains(hitPoint)) {
                return entity;
            }
        }

        return null;
    }

    public static Point2D screenToWorld(CameraComponent cc, PositionComponent pc, double x, double y) {
        return new Point2D(x / cc.scale - pc.x, y / cc.scale - pc.y);
    }
}
