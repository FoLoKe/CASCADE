package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.CollisionComponent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CollisionDebugRendererSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    private final ComponentMapper<CollisionComponent> cc = ComponentMapper.getFor(CollisionComponent.class);

    private GraphicsContext gc;

    public CollisionDebugRendererSystem(GraphicsContext graphicsContext) {
        this.gc =graphicsContext;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(CollisionComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(0.25);

        for (Entity entity : entities) {
            CollisionComponent component = cc.get(entity);
            Rectangle rectangle = component.hitBox;
            gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        }
    }
}
