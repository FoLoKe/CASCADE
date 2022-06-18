package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.CollisionComponent;
import com.foloke.cascade.Components.Tags.SelectedTag;
import com.foloke.cascade.utils.QuadCollision;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SelectionRendererSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    private final ComponentMapper<CollisionComponent> cm = ComponentMapper.getFor(CollisionComponent.class);

    private final GraphicsContext gc;

    public SelectionRendererSystem(GraphicsContext graphicsContext) {
        this.gc = graphicsContext;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(SelectedTag.class, CollisionComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(1);
        entities.forEach((entity) -> {
            CollisionComponent collisionComponent = cm.get(entity);
            QuadCollision quad = collisionComponent.hitBox;

            gc.strokeRect(quad.getX(), quad.getY(), quad.getWidth(), quad.getHeight());
        });
    }
}
